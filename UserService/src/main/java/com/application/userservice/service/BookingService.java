package com.application.userservice.service;

import com.application.userservice.dto.BookingDTO;
import com.application.userservice.dto.BusScheduleDTO;
import com.application.userservice.dto.UserDTO;
import com.application.userservice.entity.Booking;
import com.application.userservice.repository.BookingRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private BusService busService;
    @Autowired
    private RestTemplate restTemplate;

    private static final String CIRCUIT_BREAKER_NAME = "scheduleService";

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallbackGetScheduleDetails")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    public BusScheduleDTO getScheduleDetails(Long scheduleId) {
        String url = "http://ADMIN-SERVICE/api/admin/schedules/" + scheduleId;
        return restTemplate.getForObject(url, BusScheduleDTO.class);
    }

    public BusScheduleDTO fallbackGetScheduleDetails(Long scheduleId, Throwable t) {
        return new BusScheduleDTO();
    }

    @CircuitBreaker(name = "bookingService", fallbackMethod = "fallbackSaveBooking")
    public void saveBooking(Booking booking) {
        bookingRepository.save(booking);
        BusScheduleDTO scheduleDTO = getScheduleDetails(booking.getScheduleId());

        int totalSeats = scheduleDTO.getCapacity();
        int bookedSeats = bookingRepository.countByScheduleId(booking.getScheduleId());
        int availableSeats = totalSeats - bookedSeats;
        updateAvailableSeatsInAdminService(booking.getScheduleId(), availableSeats);
    }

    public void fallbackSaveBooking(Booking booking, Throwable t) {
        logger.warn("Fallback for saveBooking: " + t.getMessage());
    }

    @CircuitBreaker(name = "scheduleService", fallbackMethod = "fallbackUpdateSeats")
    public void updateAvailableSeatsInAdminService(Long scheduleId, int availableSeats) {
        String url = "http://ADMIN-SERVICE/api/admin/schedules/" + scheduleId + "/update-available-seats?availableSeats=" + availableSeats;
        restTemplate.put(url, null);
    }

    public void fallbackUpdateSeats(Long scheduleId, int availableSeats, Throwable t) {
        logger.warn("Fallback for updateAvailableSeatsInAdminService: " + t.getMessage());
    }

    public boolean isSeatAvailable(Long scheduleId, int seatNumber) {
        return !bookingRepository.existsByScheduleIdAndSeatNumber(scheduleId, seatNumber);
    }

    @CircuitBreaker(name = "bookingService", fallbackMethod = "fallbackIsSeatAvailable")
    public List<Integer> getAvailableSeats(Long scheduleId, int totalSeats) {
        List<Integer> bookedSeats = bookingRepository.findByScheduleId(scheduleId)
                .stream()
                .map(Booking::getSeatNumber)
                .collect(Collectors.toList());

        return IntStream.rangeClosed(1, totalSeats)
                .filter(seat -> !bookedSeats.contains(seat))
                .boxed()
                .collect(Collectors.toList());
    }

    public boolean fallbackIsSeatAvailable(Long scheduleId, int seatNumber, Throwable t) {
        logger.warn("Fallback for isSeatAvailable: " + t.getMessage());
        return false;
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallbackGetAllBookings")
    public List<BookingDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> fallbackGetAllBookings(Throwable t) {
        logger.warn("Fallback for getAllBookings: " + t.getMessage());
        return Collections.emptyList();
    }

    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setSeatNumber(booking.getSeatNumber());
        dto.setBookingDate(booking.getBookingDate());
        dto.setPaymentStatus(booking.getPaymentStatus());

        // Fetch user details from UserService
        String userServiceUrl = "http://USER-SERVICE/api/customer/" + booking.getUserId();
        UserDTO user = restTemplate.getForObject(userServiceUrl, UserDTO.class);
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());

        // Fetch schedule details from ScheduleService
        System.out.println("Booking scheduleid: " + booking.getScheduleId());
        String scheduleServiceUrl = "http://ADMIN-SERVICE/api/admin/schedules/" + booking.getScheduleId();
        BusScheduleDTO schedule = restTemplate.getForObject(scheduleServiceUrl, BusScheduleDTO.class);
        dto.setBusNumber(schedule.getBusNumber());
        dto.setOrigin(schedule.getOrigin());
        dto.setDestination(schedule.getDestination());
        dto.setArrivalTime(schedule.getArrivalTime());
        dto.setDepartureTime(schedule.getDepartureTime());
        dto.setFare(schedule.getFare());

        return dto;
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallbackGetBookingsByEmail")
    public List<BookingDTO> getBookingsByEmail(String email) {
        String userServiceUrl = "http://USER-SERVICE/api/customer/email?email=" + email;
        UserDTO user = restTemplate.getForObject(userServiceUrl, UserDTO.class);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }
        List<Booking> bookings = bookingRepository.findByUserId(user.getUserId());
        return bookings.stream()
                .map(booking -> convertBookingToDTO(booking, user.getEmail()))
                .collect(Collectors.toList());
    }

    public List<BookingDTO> fallbackGetBookingsByEmail(String email, Throwable t) {
        logger.warn("Fallback for getBookingsByEmail: " + t.getMessage());
        return Collections.emptyList();
    }

    private BookingDTO convertBookingToDTO(Booking booking, String email) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setEmail(email);
        dto.setSeatNumber(booking.getSeatNumber());
        dto.setBookingDate(booking.getBookingDate());
        dto.setPaymentStatus(booking.getPaymentStatus());

        String userServiceUrl = "http://USER-SERVICE/api/customer/" + booking.getUserId();
        UserDTO user = restTemplate.getForObject(userServiceUrl, UserDTO.class);
        dto.setName(user.getName());

        String scheduleServiceUrl = "http://ADMIN-SERVICE/api/admin/schedules/" + booking.getScheduleId();
        BusScheduleDTO schedule = restTemplate.getForObject(scheduleServiceUrl, BusScheduleDTO.class);
        dto.setBusNumber(schedule.getBusNumber());
        dto.setOrigin(schedule.getOrigin());
        dto.setDestination(schedule.getDestination());
        dto.setArrivalTime(schedule.getArrivalTime());
        dto.setDepartureTime(schedule.getDepartureTime());
        dto.setFare(schedule.getFare());

        return dto;
    }

    @CircuitBreaker(name = "scheduleService", fallbackMethod = "fallbackCancelBooking")
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setPaymentStatus("CANCELED");
        bookingRepository.save(booking);
        Long scheduleId = booking.getScheduleId();
        int seat = booking.getSeatNumber();
        String scheduleServiceUrl = "http://ADMIN-SERVICE/api/admin/schedules/" + scheduleId + "/update-available-seats?availableSeats=" + seat;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJkaGl2eWExMiIsImlhdCI6MTc0MTk0NTc4OCwiZXhwIjoxNzQyNTUwNTg4fQ.HKgFrSFA9iFT5M90Xhju47ZwyD5wqddp-hlWB7YYQCTIJHbqKmgYZlRhRrUA3poY");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    scheduleServiceUrl,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Available seats updated successfully");
            } else {
                throw new RuntimeException("Failed to update schedule seats. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update schedule seats: " + e.getMessage(), e);
        }
    }

    public void fallbackCancelBooking(Long id, Throwable t) {
        logger.warn("Fallback for cancelBooking: " + t.getMessage());
    }

    @CircuitBreaker(name = "bookingService", fallbackMethod = "fallbackFindBookingById")
    public Optional<Booking> findBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public Optional<Booking> fallbackFindBookingById(Long id, Throwable t) {
        logger.warn("Fallback for findBookingById: " + t.getMessage());
        return Optional.empty();
    }
}

