package com.application.userservice.service;

import com.application.userservice.dto.BusDTO;
import com.application.userservice.dto.BusScheduleDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BusService {
    @Autowired
    private RestTemplate restTemplate;

    private static final String BUS_SERVICE = "busService";
    @CircuitBreaker(name = BUS_SERVICE, fallbackMethod = "searchBusesFallback")
    public List<BusScheduleDTO> searchBuses(String origin, String destination, LocalDate date) {
        String url = "http://ADMIN-SERVICE/api/admin/search?origin={origin}&destination={destination}&date={date}";

        Map<String, String> params = new HashMap<>();
        params.put("origin", origin);
        params.put("destination", destination);
        params.put("date", date.toString());

        ResponseEntity<List<BusScheduleDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BusScheduleDTO>>() {},
                params
        );
        return response.getBody();
    }
    @CircuitBreaker(name = BUS_SERVICE, fallbackMethod = "getBusByNumberFallback")
    public BusDTO getBusByNumber(String busNumber){
        String url="http://ADMIN-SERVICE/api/admin/buses/"+busNumber;
        return restTemplate.getForObject(url,BusDTO.class);
    }
    private List<BusScheduleDTO> searchBusesFallback(String origin, String destination, LocalDate date, Throwable t) {
        System.out.println("Fallback method called for searchBuses due to: " + t.getMessage());
        return List.of();
    }
    private BusDTO getBusByNumberFallback(String busNumber, Throwable t) {
        System.out.println("Fallback method called for getBusByNumber due to: " + t.getMessage());
        return new BusDTO();
    }
}