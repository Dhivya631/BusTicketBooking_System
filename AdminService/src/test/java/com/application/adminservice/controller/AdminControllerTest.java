package com.application.adminservice.controller;

import com.application.adminservice.dto.BookingDTO;
import com.application.adminservice.entity.User;
import com.application.adminservice.service.AdminService;
import com.application.adminservice.service.BusService;
import com.application.adminservice.service.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private BusService busService;

    @MockBean
    private ScheduleService scheduleService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    @DisplayName("Display Admin Page")
    void testAdminPage() throws Exception {
        mockMvc.perform(get("/api/admin/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("adminLogin"));
    }

    @Test
    @DisplayName("Display Admin Dashboard")
    void testAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/admin/dash"))
                .andExpect(status().isOk())
                .andExpect(view().name("adminDashboard"));
    }

    @Test
    @DisplayName("Admin logined successfully")
    void testAdminLogin_Success() throws Exception {
        when(adminService.authenticateLogin("admin", "password")).thenReturn(true);

        mockMvc.perform(post("/api/admin/login")
                        .param("username", "admin")
                        .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(view().name("adminDashboard"))
                .andExpect(model().attributeExists("successMessage"));

        verify(adminService, times(1)).authenticateLogin("admin", "password");
    }

    @Test
    @DisplayName("Admin login unsuccessful")
    void testAdminLogin_Failure() throws Exception {
        when(adminService.authenticateLogin("admin", "wrongpassword")).thenReturn(false);

        mockMvc.perform(post("/api/admin/login")
                        .param("username", "admin")
                        .param("password", "wrongpassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("adminLogin"))
                .andExpect(model().attributeExists("errorMessage"));

        verify(adminService, times(1)).authenticateLogin("admin", "wrongpassword");
    }

    @Test
    @DisplayName("Get all user details")
    void testGetAllUsers() throws Exception {
        User user1=new User();
        user1.setId(1L);
        user1.setName("John");
        user1.setEmail("john@example.com");

        User user2=new User();
        user2.setId(2L);
        user2.setName("Janu");
        user2.setEmail("janu@example.com");
        List<User> users = Arrays.asList(user1,user2);
        Page<User> userPage = new PageImpl<>(users);
        when(adminService.getAllUserFromUserService(anyInt(), anyInt(), anyString())).thenReturn(userPage);

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "5")
                        .param("search", "John"))
                .andExpect(status().isOk())
                .andExpect(view().name("viewUser"))
                .andExpect(model().attributeExists("users", "currentPage", "totalPages", "search"));

        verify(adminService, times(1)).getAllUserFromUserService(anyInt(), anyInt(), anyString());
    }

    @Test
    @DisplayName("View all booking history")
    void testViewBooking() throws Exception {
        BookingDTO bookingDTO=new BookingDTO();
        bookingDTO.setId(1L);
        bookingDTO.setName("Dhivya");
        bookingDTO.setEmail("dhivya@gmail.com");
        bookingDTO.setBusNumber("Bus 101");
        List<BookingDTO> bookings = Arrays.asList(bookingDTO);
        when(adminService.getAllBooking()).thenReturn(bookings);

        mockMvc.perform(get("/api/admin/booking-history"))
                .andExpect(status().isOk())
                .andExpect(view().name("bookingHistory"))
                .andExpect(model().attributeExists("bookings"));

        verify(adminService, times(1)).getAllBooking();
    }

    @Test
    @DisplayName("Cancel Booking")
    void testCancelBooking() throws Exception {
        doNothing().when(adminService).cancelBooking(1L);

        mockMvc.perform(post("/api/admin/booking/1/cancel")
                        .param("redirecturl", "/api/admin/booking-history"))
                .andExpect(status().is3xxRedirection())

                .andExpect(redirectedUrl("/api/admin/booking-history"));

        verify(adminService, times(1)).cancelBooking(1L);
    }
}