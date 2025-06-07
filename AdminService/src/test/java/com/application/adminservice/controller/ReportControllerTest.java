package com.application.adminservice.controller;

import com.application.adminservice.dto.BookingDTO;
import com.application.adminservice.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ReportController.class)
class ReportControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AdminService adminService;

    @MockBean
    private ResourceLoader resourceLoader;

    @Test
    @DisplayName("Show reporting page")
    void testShowReportPage() throws Exception {
        mockMvc.perform(get("/api/admin/reports/book"))
                .andExpect(status().isOk())
                .andExpect(view().name("report"));
    }

    @Test
    @DisplayName("Generate pdf report")
    void testGeneratePdfReport() throws Exception {
        BookingDTO bookingDTO=new BookingDTO();
        bookingDTO.setId(1L);
        bookingDTO.setName("Dhivya");
        bookingDTO.setEmail("dhivya@gmail.com");
        bookingDTO.setBusNumber("Bus 101");
        List<BookingDTO> mockBookings = List.of(bookingDTO);
        when(adminService.getAllBooking()).thenReturn(mockBookings);

        mockMvc.perform(get("/api/admin/reports/booking-summary/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=booking-summary.pdf"));
    }

    @Test
    @DisplayName("Generate excel report")
    void testGenerateExcelReport() throws Exception {
        BookingDTO bookingDTO=new BookingDTO();
        bookingDTO.setId(1L);
        bookingDTO.setName("Dhivya");
        bookingDTO.setEmail("dhivya@gmail.com");
        bookingDTO.setBusNumber("Bus 101");
        List<BookingDTO> mockBookings = List.of(bookingDTO);
        when(adminService.getAllBooking()).thenReturn(mockBookings);

        mockMvc.perform(get("/api/admin/reports/booking-summary/excel"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.ms-excel"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=booking-summary.xls"));
    }
}