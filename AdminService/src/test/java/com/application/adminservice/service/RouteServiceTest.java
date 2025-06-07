package com.application.adminservice.service;

import com.application.adminservice.entity.Route;
import com.application.adminservice.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {
    @Mock
    private RouteRepository routeRepository;
    @InjectMocks
    private RouteService routeService;

    private Route route1;
    private Route route2;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        route1 = new Route();
        route1.setRouteId(1L);
        route1.setOrigin("CityA");
        route1.setDestination("CityB");

        route2 = new Route();
        route2.setRouteId(2L);
        route2.setOrigin("CityB");
        route2.setDestination("CityC");
        pageable = PageRequest.of(0, 2, Sort.by("origin").ascending());
    }

    @Test
    @DisplayName("Find routes using both origin and destination")
    void testFindRoutesByOriginAndDestination_BothProvided() {
        Page<Route> mockPage = new PageImpl<>(Arrays.asList(route1, route2));

        when(routeRepository.findByOriginContainingAndDestinationContaining("CityA", "CityB", pageable))
                .thenReturn(mockPage);

        Page<Route> result = routeService.findRoutesByOriginAndDestination("CityA", "CityB", pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(routeRepository, times(1))
                .findByOriginContainingAndDestinationContaining("CityA", "CityB", pageable);
    }

    @Test
    @DisplayName("Find routes using origin")
    void testFindRoutesByOriginAndDestination_OnlyOriginProvided() {
        Page<Route> mockPage = new PageImpl<>(Collections.singletonList(route1));

        when(routeRepository.findByOriginContaining("CityA", pageable)).thenReturn(mockPage);

        Page<Route> result = routeService.findRoutesByOriginAndDestination("CityA", null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(routeRepository, times(1)).findByOriginContaining("CityA", pageable);
    }

    @Test
    @DisplayName("Find routes using destination")
    void testFindRoutesByOriginAndDestination_OnlyDestinationProvided() {
        Page<Route> mockPage = new PageImpl<>(Collections.singletonList(route2));

        when(routeRepository.findByDestinationContaining("CityC", pageable)).thenReturn(mockPage);

        Page<Route> result = routeService.findRoutesByOriginAndDestination(null, "CityC", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(routeRepository, times(1)).findByDestinationContaining("CityC", pageable);
    }

    @Test
    @DisplayName("Find routes using origin and destination with no filters")
    void testFindRoutesByOriginAndDestination_NoFilters() {
        Page<Route> mockPage = new PageImpl<>(Arrays.asList(route1, route2));

        when(routeRepository.findAll(pageable)).thenReturn(mockPage);

        Page<Route> result = routeService.findRoutesByOriginAndDestination(null, null, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(routeRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Get all routes")
    void testGetAllRoutes_Success() {
        List<Route> mockRoutes = Arrays.asList(route1, route2);

        when(routeRepository.findAll()).thenReturn(mockRoutes);

        List<Route> result = routeService.getAllRoutes();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(routeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Fallback to get all routes")
    void testGetAllRoutesFallback() {
        List<Route> fallbackResult = routeService.getAllRoutesFallback(new RuntimeException("Database error"));

        assertNotNull(fallbackResult);
        assertTrue(fallbackResult.isEmpty());
    }

    @Test
    @DisplayName("Find routes fallback")
    void testFindRoutesFallback() {
        Page<Route> fallbackResult = routeService.findRoutesFallback("CityA", "CityB", pageable, new RuntimeException("Database error"));

        assertNotNull(fallbackResult);
        assertTrue(fallbackResult.isEmpty());
    }
}