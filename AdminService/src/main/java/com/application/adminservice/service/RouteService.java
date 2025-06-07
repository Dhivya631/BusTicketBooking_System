package com.application.adminservice.service;

import com.application.adminservice.entity.Route;
import com.application.adminservice.repository.RouteRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class RouteService {
    @Autowired
    private RouteRepository routeRepository;

    private static final String ROUTE_SERVICE = "routeService";

    @CircuitBreaker(name = ROUTE_SERVICE, fallbackMethod = "findRoutesFallback")
    public Page<Route> findRoutesByOriginAndDestination(String origin, String destination, Pageable pageable) {
        if (origin != null && destination != null) {
            return routeRepository.findByOriginContainingAndDestinationContaining(origin, destination, pageable);
        } else if (origin != null) {
            return routeRepository.findByOriginContaining(origin, pageable);
        } else if (destination != null) {
            return routeRepository.findByDestinationContaining(destination, pageable);
        } else {
            return routeRepository.findAll(pageable);
        }
    }

    @CircuitBreaker(name = ROUTE_SERVICE, fallbackMethod = "getAllRoutesFallback")
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }
    public List<Route> getAllRoutesFallback(Throwable t) {
        System.err.println("Circuit breaker activated for getAllRoutes(): " + t.getMessage());
        return Collections.emptyList();
    }
    public Page<Route> findRoutesFallback(String origin, String destination, Pageable pageable, Throwable t) {
        System.err.println("Circuit breaker activated for findRoutesByOriginAndDestination(): " + t.getMessage());
        return Page.empty();
    }
}