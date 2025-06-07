package com.application.adminservice.service;

import com.application.adminservice.dto.BusDTO;
import com.application.adminservice.dto.BusScheduleDto;
import com.application.adminservice.entity.Bus;
import com.application.adminservice.entity.Route;
import com.application.adminservice.entity.Schedule;
import com.application.adminservice.repository.BusRepository;
import com.application.adminservice.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusServiceTest {
    @Mock
    private BusRepository busRepository;
    @Mock
    private ScheduleRepository scheduleRepository;
    @InjectMocks
    private BusService busService;
    private Bus bus;
    private Schedule schedule;
    private Route route;

    @BeforeEach
    void setUp() {
        bus = new Bus();
        bus.setBusNumber("BUS123");
        bus.setBusType("AC");
        bus.setCapacity(40);

        route = new Route();
        route.setOrigin("CityA");
        route.setDestination("CityB");

        schedule = new Schedule();
        schedule.setScheduleId(1L);
        schedule.setBus(bus);
        schedule.setRoute(route);
        schedule.setDate(LocalDate.now());
        schedule.setDepartureTime(LocalDateTime.of(2025,3,15,10,0));
        schedule.setArrivalTime(LocalDateTime.of(2025,3,15,12,0));
        schedule.setFare(500);
    }

    @Test
    @DisplayName("Saved bus details")
    void testSaveBus() {
        when(busRepository.save(any(Bus.class))).thenReturn(bus);
        Bus savedBus = busService.saveBus(bus);

        assertNotNull(savedBus);
        assertEquals("BUS123", savedBus.getBusNumber());
        verify(busRepository, times(1)).save(bus);
    }

    @Test
    @DisplayName("Get all buses with filter")
    void testGetAllBuses_WithFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Bus> busList = Collections.singletonList(bus);
        Page<Bus> page = new PageImpl<>(busList);

        when(busRepository.findByBusNumberContainingIgnoreCase("BUS", pageable)).thenReturn(page);

        Page<Bus> result = busService.getAllBuses(0, 10, "BUS");

        assertEquals(1, result.getTotalElements());
        assertEquals("BUS123", result.getContent().get(0).getBusNumber());
        verify(busRepository, times(1)).findByBusNumberContainingIgnoreCase("BUS", pageable);
    }

    @Test
    @DisplayName("Get all bus with no filter")
    void testGetAllBuses_NoFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Bus> busList = Collections.singletonList(bus);
        Page<Bus> page = new PageImpl<>(busList);

        when(busRepository.findAll(pageable)).thenReturn(page);

        Page<Bus> result = busService.getAllBuses(0, 10, null);

        assertEquals(1, result.getTotalElements());
        verify(busRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Get all bus")
    void testGetAllBuse() {
        when(busRepository.findAll()).thenReturn(Collections.singletonList(bus));

        List<Bus> result = busService.getAllBuse();

        assertEquals(1, result.size());
        assertEquals("BUS123", result.get(0).getBusNumber());
        verify(busRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Serach buses")
    void testSearchBuses() {
        when(scheduleRepository.findByRouteOriginAndRouteDestinationAndDate("CityA", "CityB", LocalDate.now()))
                .thenReturn(Collections.singletonList(schedule));
        when(scheduleRepository.countByScheduleId(schedule.getScheduleId())).thenReturn(5);

        List<BusScheduleDto> result = busService.searchBuses("CityA", "CityB", LocalDate.now());

        assertEquals(1, result.size());
        assertEquals("CityA", result.get(0).getOrigin());
        assertEquals(35, result.get(0).getAvailableSeats()); // 40 total - 5 booked
        verify(scheduleRepository, times(1)).findByRouteOriginAndRouteDestinationAndDate("CityA", "CityB", LocalDate.now());
    }

    @Test
    @DisplayName("Get bus details using bus number")
    void testGetBusByNumber() {
        when(busRepository.findByBusNumber("BUS123")).thenReturn(Optional.of(bus));
        when(scheduleRepository.findSchedulesByBusNumber("BUS123")).thenReturn(Collections.singletonList(schedule));
        when(scheduleRepository.countByScheduleId(schedule.getScheduleId())).thenReturn(5);

        BusDTO result = busService.getBusByNumber("BUS123");

        assertNotNull(result);
        assertEquals("BUS123", result.getBusNumber());
        assertEquals(1, result.getSchedules().size());
        assertEquals(35, result.getSchedules().get(0).getAvailableSeats());
        verify(busRepository, times(1)).findByBusNumber("BUS123");
        verify(scheduleRepository, times(1)).findSchedulesByBusNumber("BUS123");
    }

    @Test
    @DisplayName("Fallback for save bus")
    void testFallbackSaveBus() {
        Bus fallbackBus = busService.fallbackSaveBus(bus, new RuntimeException("Service unavailable"));

        assertNotNull(fallbackBus);
        assertNull(fallbackBus.getBusNumber()); // Should return an empty Bus object
    }

    @Test
    @DisplayName("Fallback for get all the bus")
    void testFallbackGetAllBuses() {
        Page<Bus> fallbackPage = busService.fallbackGetAllBuses(0, 10, "BUS", new RuntimeException("Service unavailable"));

        assertNotNull(fallbackPage);
        assertTrue(fallbackPage.isEmpty());
    }

    @Test
    void testFallbackGetAllBuse() {
        List<Bus> fallbackList = busService.fallbackGetAllBuse(new RuntimeException("Service unavailable"));

        assertNotNull(fallbackList);
        assertTrue(fallbackList.isEmpty());
    }

    @Test
    @DisplayName("Fallback for search buses")
    void testFallbackSearchBuses() {
        List<BusScheduleDto> fallbackList = busService.fallbackSearchBuses("CityA", "CityB", LocalDate.now(), new RuntimeException("Service unavailable"));

        assertNotNull(fallbackList);
        assertTrue(fallbackList.isEmpty());
    }

    @Test
    @DisplayName("Falback for get bus details using bus number")
    void testFallbackGetBusByNumber() {
        BusDTO fallbackDTO = busService.fallbackGetBusByNumber("BUS123", new RuntimeException("Service unavailable"));

        assertNotNull(fallbackDTO);
        assertEquals("N/A", fallbackDTO.getBusNumber());
        assertEquals("Unknown", fallbackDTO.getBusType());
        assertTrue(fallbackDTO.getSchedules().isEmpty());
    }
}