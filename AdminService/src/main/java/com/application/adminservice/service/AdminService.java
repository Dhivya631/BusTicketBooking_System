package com.application.adminservice.service;

import com.application.adminservice.dto.BookingDTO;
import com.application.adminservice.entity.User;
import com.application.adminservice.entity.UserResponse;
import com.application.adminservice.repository.BusRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class AdminService {
    private static final String USER_SERVICE_URL = "http://USER-SERVICE/api/customer/viewAllUser";
    private static final String URL = "http://USER-SERVICE/api/booking/viewAllBooking";

    @Value("${admin.username}")
    private String adminUsername;
    @Value("${admin.password}")
    private String adminPassword;
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private RestTemplate restTemplate;

    public boolean authenticateLogin(String username, String password) {
        return username.equals(adminUsername) && password.equals(adminPassword);
    }
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetAllUsers")
    @Retry(name = "userService")
    public Page<User> getAllUserFromUserService(int page, int size, String search) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJkaGl2eWExMiIsImlhdCI6MTc0MDY0ODk1MywiZXhwIjoxNzQxMjUzNzUzfQ.45L43xwSe9gPgxZ7I6Lkp1d5AJAI0FaBsMWB-uifLYqqo-t0uyiGQnItIIeNur6j");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Construct the URL with the search parameter if it is provided
        String url = USER_SERVICE_URL + "?page=" + page + "&size=" + size;
        if (search != null && !search.isEmpty()) {
            url += "&search=" + search; // Add the search parameter to the URL
        }
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UserResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            UserResponse userResponse = response.getBody();
            List<User> users = userResponse.getContent();
            long totalElements = userResponse.getTotalElements();
            return new PageImpl<>(users, PageRequest.of(page, size), totalElements);
        } else {
            throw new RuntimeException("Failed to fetch users: " + response.getStatusCode());
        }
    }
    public Page<User> fallbackGetAllUsers(int page, int size, String search, Throwable throwable) {
        System.err.println("Fallback method triggered: " + throwable.getMessage());
        return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
    }

    @CircuitBreaker(name = "bookingService", fallbackMethod = "fallbackGetAllBookings")
    @Retry(name = "bookingService")
    public List<BookingDTO> getAllBooking() {
        BookingDTO[] bookings = restTemplate.getForObject(URL, BookingDTO[].class);
        return Arrays.asList(bookings);
    }
    public List<BookingDTO> fallbackGetAllBookings(Throwable throwable) {
        System.err.println("Fallback method triggered: " + throwable.getMessage());
        return Collections.emptyList();
    }

    @CircuitBreaker(name = "cancelBookingService", fallbackMethod = "fallbackCancelBooking")
    @Retry(name = "cancelBookingService")
    public void cancelBooking(Long id) {
        String url = "http://USER-SERVICE/api/booking/" + id + "/cancel";
        System.out.println("Calling URL: " + url);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Booking cancelled successfully");
            } else {
                throw new RuntimeException("Failed to cancel booking. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error cancelling booking: " + e.getMessage());
            throw new RuntimeException("Failed to cancel booking: " + e.getMessage(), e);
        }
    }
    public void fallbackCancelBooking(Long id, Throwable throwable) {
        System.err.println("Fallback triggered for cancelBooking: " + throwable.getMessage());
    }

}
