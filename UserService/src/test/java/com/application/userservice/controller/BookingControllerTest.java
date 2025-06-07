package com.application.userservice.controller;

import com.application.userservice.configuration.JwtTokenProvider;
import com.application.userservice.configuration.JwtAuthenticationFilter;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @MockBean
    private BookingService bookingService;
    @MockBean
    private BookingRepository bookingRepository;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private BusService busService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
    }

    @Test
    @DisplayName("Display Book bus page - User not logined")
    void testShowBookBusPage_UserNotLoggedIn_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/api/booking/book-bus").param("busNumber", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/customer/login"));
    }
    @Test
    @DisplayName("Logined user")
    void testShowBookBusPage_LoggedInUser_ShouldReturnBookingForm() throws Exception {
        User user = new User();
        user.setUsername("testUser");
        user.setId(1L);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        BusDTO busDTO = new BusDTO();
        busDTO.setBusNumber("123");
        busDTO.setCapacity(40);
        busDTO.setSchedules(Collections.singletonList(new BusScheduleDTO()));

        lenient().when(busService.getBusByNumber("123")).thenReturn(busDTO);
        lenient().when(bookingService.getAvailableSeats(1L, 40)).thenReturn(Arrays.asList(1, 2, 3));

        mockMvc.perform(get("/api/booking/book-bus").param("busNumber", "123").sessionAttr("username", "testUser"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking-form"))
                .andExpect(model().attributeExists("bus"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("availableSeats"));
    }
    @Test
    @DisplayName("Booked bus successfully")
    void testBookBus_SuccessfulBooking_ShouldRedirectToConfirmation() throws Exception {
        when(bookingService.isSeatAvailable(1L, 5)).thenReturn(true);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setScheduleId(1L);
        booking.setSeatNumber(5);
        booking.setUserId(1L);

        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        BusDTO busDTO = new BusDTO();
        busDTO.setBusNumber("123");
        busDTO.setCapacity(40);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        doNothing().when(bookingService).saveBooking(any(Booking.class));

        mockMvc.perform(post("/api/booking/book")
                        .param("busNumber", "123")
                        .param("scheduleId", "1")
                        .param("seatNumber", "5")
                        .sessionAttr("username", "testUser"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Booking confirmation")
    void testShowConfirmationPage_ShouldReturnConfirmationView() throws Exception {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUserId(2L);
        booking.setScheduleId(3L);
        booking.setBookingDate(LocalDate.now());
        booking.setSeatNumber(10);
        booking.setPaymentStatus("CONFIRMED");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        UserDTO user = new UserDTO();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        when(restTemplate.getForObject("http://localhost:8081/api/customer/2", UserDTO.class)).thenReturn(user);

        BusScheduleDTO schedule = new BusScheduleDTO();
        schedule.setBusNumber("123");
        schedule.setOrigin("CityA");
        schedule.setDestination("CityB");
        when(restTemplate.getForObject("http://localhost:8082/api/admin/schedules/3", BusScheduleDTO.class)).thenReturn(schedule);

        mockMvc.perform(get("/api/booking/confirmation/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("booking-confirmation"))
                .andExpect(model().attributeExists("booking"));
    }

    @Test
    @DisplayName("View all booking")
    void testViewAllBooking_ShouldReturnBookingList() throws Exception {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(1L);
        bookingDTO.setName("John Doe");

        List<BookingDTO> bookings = Collections.singletonList(bookingDTO);
        when(bookingService.getAllBookings()).thenReturn(bookings);

        mockMvc.perform(get("/api/booking/viewAllBooking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    @DisplayName("Cancel booking")
    void testCancelBooking_Success_ShouldReturnSuccessMessage() throws Exception {
        when(bookingService.findBookingById(1L)).thenReturn(Optional.of(new Booking()));

        mockMvc.perform(post("/api/booking/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().string("Booking canceled successfully."));
    }

    @Test
    @DisplayName("Cancel booking -Not Found")
    void testCancelBooking_NotFound_ShouldReturnNotFoundMessage() throws Exception {
        when(bookingService.findBookingById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/booking/1/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Booking not found with ID: 1"));
    }
}