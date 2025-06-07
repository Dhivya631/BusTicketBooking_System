package com.application.userservice.service;

import com.application.userservice.dto.BusDTO;
import com.application.userservice.dto.BusScheduleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BusService busService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Search Buses - Success")
    void testSearchBuses_Success() {
        String origin = "CityA";
        String destination = "CityB";
        LocalDate date = LocalDate.of(2025, 3, 18);

        BusScheduleDTO busScheduleDTO = new BusScheduleDTO();
        busScheduleDTO.setBusNumber("BUS123");
        busScheduleDTO.setOrigin(origin);
        busScheduleDTO.setDestination(destination);

        List<BusScheduleDTO> expectedResponse = List.of(busScheduleDTO);
        ResponseEntity<List<BusScheduleDTO>> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                anyMap()
        )).thenReturn(responseEntity);

        List<BusScheduleDTO> result = busService.searchBuses(origin, destination, date);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("BUS123", result.get(0).getBusNumber());
    }

    @Test
    @DisplayName("Get Bus By Number - Success")
    void testGetBusByNumber_Success() {
        String busNumber = "BUS123";
        BusDTO expectedBus = new BusDTO();
        expectedBus.setBusNumber(busNumber);
        expectedBus.setCapacity(50);

        when(restTemplate.getForObject(anyString(), eq(BusDTO.class)))
                .thenReturn(expectedBus);

        BusDTO result = busService.getBusByNumber(busNumber);

        assertNotNull(result);
        assertEquals(busNumber, result.getBusNumber());
        assertEquals(50, result.getCapacity());
    }
}