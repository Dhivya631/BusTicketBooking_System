package com.application.adminservice.service;

import com.application.adminservice.dto.BookingDTO;
import com.application.adminservice.repository.BusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private BusRepository busRepository;

    private static final String USER_SERVICE_URL = "http://localhost:8081/api/customer/viewAllUser";
    private static final String BOOKING_URL = "http://localhost:8081/api/booking/viewAllBooking";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminService, "adminUsername", "admin");
        ReflectionTestUtils.setField(adminService, "adminPassword", "password");
    }
    @Test
    @DisplayName("Valid credentials")
    void testAuthenticateLogin_ValidCredentials() {
        assertTrue(adminService.authenticateLogin("admin", "password"));
    }

    @Test
    @DisplayName("Valid credentials")
    void testAuthenticateLogin_InvalidCredentials() {
        assertFalse(adminService.authenticateLogin("user", "wrongPass"));
    }
    @Test
    @DisplayName("Get all booking")
    void testGetAllBooking_Success() {
        BookingDTO bookingDTO=new BookingDTO();
        bookingDTO.setId(1L);
        bookingDTO.setName("Dhivya");
        bookingDTO.setEmail("dhivya@gmail.com");
        bookingDTO.setBusNumber("Bus 101");

        BookingDTO bookingDTO1=new BookingDTO();
        bookingDTO.setId(1L);
        BookingDTO[] mockBookings = {bookingDTO,bookingDTO1};
        when(restTemplate.getForObject(BOOKING_URL, BookingDTO[].class)).thenReturn(mockBookings);

        List<BookingDTO> result = adminService.getAllBooking();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Dhivya", result.get(0).getName());
    }

    @Test
    @DisplayName("Cancel booking")
    void testCancelBooking_Success() {
        Long bookingId = 1L;
        String cancelUrl = "http://localhost:8081/api/booking/" + bookingId + "/cancel";

        ResponseEntity<String> responseEntity = new ResponseEntity<>("Booking Cancelled", HttpStatus.OK);
        when(restTemplate.postForEntity(cancelUrl, null, String.class)).thenReturn(responseEntity);

        assertDoesNotThrow(() -> adminService.cancelBooking(bookingId));
    }

    @Test
    @DisplayName("Cancel booking - Failure")
    void testCancelBooking_Failure() {
        Long bookingId = 1L;
        String cancelUrl = "http://localhost:8081/api/booking/" + bookingId + "/cancel";

        when(restTemplate.postForEntity(cancelUrl, null, String.class)).thenThrow(new RuntimeException("Service Down"));

        assertThrows(RuntimeException.class, () -> adminService.cancelBooking(bookingId));
    }

    @Test
    @DisplayName("Fallback for cancel booking")
    void testFallbackCancelBooking() {
        assertDoesNotThrow(() -> adminService.fallbackCancelBooking(1L, new RuntimeException("Service Down")));
    }
}