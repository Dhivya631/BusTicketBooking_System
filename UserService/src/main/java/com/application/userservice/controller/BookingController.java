package com.application.userservice.controller;

import com.application.userservice.dto.BookingDTO;
import com.application.userservice.dto.BusDTO;
import com.application.userservice.dto.BusScheduleDTO;
import com.application.userservice.dto.UserDTO;
import com.application.userservice.entity.Booking;
import com.application.userservice.entity.User;
import com.application.userservice.repository.BookingRepository;
import com.application.userservice.repository.UserRepository;
import com.application.userservice.service.BookingService;
import com.application.userservice.service.BusService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/api/booking")
public class BookingController {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private BusService busService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/book-bus")
    public String showBookBusPage(@RequestParam String busNumber, Model model,HttpSession session) {
        String username=(String) session.getAttribute("username");
        if(username==null){
            return "redirect:/api/customer/login";
        }
        User user=userRepository.findByUsername(username).orElseThrow(()-> new RuntimeException("User not found"));
        BusDTO bus = busService.getBusByNumber(busNumber);
        List<BusScheduleDTO> schedules = bus.getSchedules();

        // Fetch available seats for the selected schedule
        List<Integer> availableSeats = bookingService.getAvailableSeats(schedules.get(0).getScheduleId(), bus.getCapacity());

        model.addAttribute("bus", bus);
        model.addAttribute("user",user);
        model.addAttribute("availableSeats", availableSeats);
        return "booking-form";
    }

    @PostMapping("/book")
    public String bookBus(
            @RequestParam String busNumber,
            @RequestParam Long scheduleId,
            @RequestParam int seatNumber,
            HttpSession session,
            Model model) {
        String username = (String) session.getAttribute("username");
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        if (!bookingService.isSeatAvailable(scheduleId, seatNumber)) {
            model.addAttribute("errorMessage", "Seat number " + seatNumber + " is already booked.");
            return "redirect:/api/booking/book-bus?busNumber=" + busNumber;
        }
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setScheduleId(scheduleId);
        booking.setSeatNumber(seatNumber);
        booking.setBookingDate(LocalDate.now());
        booking.setPaymentStatus("CONFIRMED");
        booking.setPnr(UUID.randomUUID().toString());

        bookingService.saveBooking(booking);
        model.addAttribute("successMessage", "Booking successful!");
        return "redirect:/api/booking/confirmation/"+booking.getId();
    }
    @GetMapping("/confirmation/{id}")
    public String showConfirmationPage(@PathVariable Long id, Model model) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        String userServiceUrl = "http://USER-SERVICE/api/customer/" + booking.getUserId();
        UserDTO user = restTemplate.getForObject(userServiceUrl, UserDTO.class);

        String scheduleServiceUrl = "http://ADMIN-SERVICE/api/admin/schedules/" + booking.getScheduleId();
        BusScheduleDTO schedule = restTemplate.getForObject(scheduleServiceUrl, BusScheduleDTO.class);

        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(booking.getId());
        bookingDTO.setName(user.getName());
        bookingDTO.setEmail(user.getEmail());
        bookingDTO.setBusNumber(schedule.getBusNumber());
        bookingDTO.setOrigin(schedule.getOrigin());
        bookingDTO.setDestination(schedule.getDestination());
        bookingDTO.setArrivalTime(schedule.getArrivalTime());
        bookingDTO.setDepartureTime(schedule.getDepartureTime());
        bookingDTO.setBookingDate(booking.getBookingDate());
        bookingDTO.setSeatNumber(booking.getSeatNumber());
        bookingDTO.setPaymentStatus(booking.getPaymentStatus());
        bookingDTO.setFare(schedule.getFare());
        model.addAttribute("booking", bookingDTO);
        return "booking-confirmation";
    }
    @GetMapping("/viewAllBooking")
    public ResponseEntity<List<BookingDTO>> viewBook(Model model){
        List<BookingDTO> bookings=bookingService.getAllBookings();
        model.addAttribute("bookings",bookings);
        return ResponseEntity.ok(bookings);
    }
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        Booking booking=bookingRepository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
        String userServiceUrl = "http://USER-SERVICE/api/customer/" + booking.getUserId();
        UserDTO user = restTemplate.getForObject(userServiceUrl, UserDTO.class);

        // Fetch schedule details from ScheduleService
        String scheduleServiceUrl = "http://ADMIN-SERVICE/api/admin/schedules/" + booking.getScheduleId();
        BusScheduleDTO schedule = restTemplate.getForObject(scheduleServiceUrl, BusScheduleDTO.class);
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(booking.getId());
        bookingDTO.setName(user.getName());
        bookingDTO.setEmail(user.getEmail());
        bookingDTO.setBusNumber(schedule.getBusNumber());
        bookingDTO.setOrigin(schedule.getOrigin());
        bookingDTO.setDestination(schedule.getDestination());
        bookingDTO.setArrivalTime(schedule.getArrivalTime());
        bookingDTO.setDepartureTime(schedule.getDepartureTime());
        bookingDTO.setBookingDate(booking.getBookingDate());
        bookingDTO.setSeatNumber(booking.getSeatNumber());
        bookingDTO.setPaymentStatus(booking.getPaymentStatus());
        bookingDTO.setFare(schedule.getFare());
        return ResponseEntity.ok(bookingDTO);
    }

    @GetMapping("/booking-history")
    public String viewBooking(@RequestParam(required = false) String email, Model model) {
        if (email != null) {
            List<BookingDTO> bookings = bookingService.getBookingsByEmail(email);
            model.addAttribute("bookings", bookings);
        }
        return "viewBooking";
    }
    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancelBooking(@PathVariable Long id) {
        try {
            System.out.println("Received request to cancel booking with ID: " + id);
            Optional<Booking> booking = bookingService.findBookingById(id);
            if (booking.isPresent()) {
                bookingService.cancelBooking(id);
                return ResponseEntity.ok("Booking canceled successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found with ID: " + id);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to cancel booking: " + e.getMessage());
        }
    }
}
