package com.application.userservice.service;

import com.application.userservice.dto.BookingDTO;
import com.application.userservice.dto.BusScheduleDTO;
import com.application.userservice.dto.UserDTO;
import com.application.userservice.entity.Booking;
import com.application.userservice.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BusService busService;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Get all schedule details")
    void testGetScheduleDetails_Success() {
        Long scheduleId = 1L;
        BusScheduleDTO mockSchedule = new BusScheduleDTO();
        mockSchedule.setCapacity(40);

        when(restTemplate.getForObject(anyString(), eq(BusScheduleDTO.class)))
                .thenReturn(mockSchedule);

        BusScheduleDTO result = bookingService.getScheduleDetails(scheduleId);
        assertNotNull(result);
        assertEquals(40, result.getCapacity());
    }

    @Test
    @DisplayName("Fallback for get all schedule details")
    void testGetScheduleDetails_Fallback() {
        Long scheduleId = 1L;

        BusScheduleDTO result = bookingService.fallbackGetScheduleDetails(scheduleId, new RuntimeException());
        assertNotNull(result);
    }

    @Test
    @DisplayName("Saved booking bus successfully")
    void testSaveBooking_Success() {
        Booking booking = new Booking();
        booking.setScheduleId(1L);

        BusScheduleDTO mockSchedule = new BusScheduleDTO();
        mockSchedule.setCapacity(50);

        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingRepository.countByScheduleId(anyLong())).thenReturn(10);
        when(restTemplate.getForObject(anyString(), eq(BusScheduleDTO.class)))
                .thenReturn(mockSchedule);
        doNothing().when(restTemplate).put(anyString(), any());
        bookingService.saveBooking(booking);

        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    @DisplayName("Get all availableSeats")
    void testGetAvailableSeats_Success() {
        Long scheduleId = 1L;
        Booking booking=new Booking();
        booking.setId(1L);
        booking.setSeatNumber(5);
        booking.setScheduleId(scheduleId);
        when(bookingRepository.findByScheduleId(scheduleId))
                .thenReturn(Arrays.asList(booking));

        List<Integer> availableSeats = bookingService.getAvailableSeats(scheduleId, 10);
        assertNotNull(availableSeats);
        assertTrue(availableSeats.contains(1));
        assertFalse(availableSeats.contains(5));
    }
    @Test
    @DisplayName("Get all booking successfully")
    void testGetAllBookings_Success() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(1L);
        userDTO.setName("dhivya");
        userDTO.setEmail("dhivya@example.com");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setSeatNumber(10);
        booking.setUserId(1L);
        booking.setScheduleId(1L);

        BusScheduleDTO scheduleDTO = new BusScheduleDTO();
        scheduleDTO.setBusNumber("BUS123");
        scheduleDTO.setOrigin("City A");
        scheduleDTO.setDestination("City B");
        scheduleDTO.setArrivalTime(LocalDateTime.of(2025,03,19,10,0));
        scheduleDTO.setDepartureTime(LocalDateTime.of(2025,03,19,12,0));
        scheduleDTO.setFare(100.0);

        List<Booking> bookings = List.of(booking);
        when(bookingRepository.findAll()).thenReturn(bookings);

        String userServiceUrl = "http://localhost:8081/api/customer/" + booking.getUserId();
        when(restTemplate.getForObject(userServiceUrl, UserDTO.class)).thenReturn(userDTO);

        String scheduleServiceUrl = "http://localhost:8082/api/admin/schedules/" + booking.getScheduleId();
        when(restTemplate.getForObject(scheduleServiceUrl, BusScheduleDTO.class)).thenReturn(scheduleDTO);

        List<BookingDTO> result = bookingService.getAllBookings();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("dhivya", result.get(0).getName());
    }


    @Test
    @DisplayName("Fallback for get all bookings")
    void testGetAllBookings_Fallback() {
        List<BookingDTO> result = bookingService.fallbackGetAllBookings(new RuntimeException("Service down"));
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Cancel booking")
    void testCancelBooking_Success() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setScheduleId(2L);
        booking.setSeatNumber(5);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("Success"));

        bookingService.cancelBooking(1L);

        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Fallback for cancel booking")
    void testCancelBooking_Fallback() {
        bookingService.fallbackCancelBooking(1L, new RuntimeException("Service unavailable"));
    }

    @Test
    @DisplayName("Get booking details using id")
    void testFindBookingById_Success() {
        Booking booking = new Booking();
        booking.setId(1L);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Optional<Booking> result = bookingService.findBookingById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("Fallback for finding booking details using id")
    void testFindBookingById_Fallback() {
        Optional<Booking> result = bookingService.fallbackFindBookingById(1L, new RuntimeException("Service unavailable"));
        assertTrue(result.isEmpty());
    }
}