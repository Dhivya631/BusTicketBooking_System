package com.application.userservice.controller;

import com.application.userservice.configuration.JwtTokenProvider;
import com.application.userservice.dto.BusScheduleDTO;
import com.application.userservice.service.BusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(BusController.class)
class BusControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private BusService busService;

    @Test
    @DisplayName("Search bus with parameters")
    void testSearchBuses_WithParams_ShouldReturnBusList() throws Exception {
        String origin = "Chennai";
        String destination = "Tirchy";
        LocalDate date = LocalDate.of(2025, 3, 20);

        BusScheduleDTO bus = new BusScheduleDTO();
        bus.setBusNumber("NY123");
        bus.setOrigin(origin);
        bus.setDestination(destination);
        List<BusScheduleDTO> mockBuses = Collections.singletonList(bus);

        Mockito.when(busService.searchBuses(origin, destination, date)).thenReturn(mockBuses);

        mockMvc.perform(get("/api/buses/search-buses")
                        .param("origin", origin)
                        .param("destination", destination)
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("search-buses"))
                .andExpect(model().attributeExists("buses"))
                .andExpect(model().attribute("buses", mockBuses));

        Mockito.verify(busService).searchBuses(origin, destination, date);
    }

    @Test
    @DisplayName("Search bus without param")
    void testSearchBuses_WithoutParams_ShouldReturnEmptyModel() throws Exception {
        mockMvc.perform(get("/api/buses/search-buses"))
                .andExpect(status().isOk())
                .andExpect(view().name("search-buses"))
                .andExpect(model().attributeDoesNotExist("buses"));

        Mockito.verifyNoInteractions(busService);
    }
}