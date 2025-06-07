package com.application.adminservice.service;

import com.application.adminservice.dto.BusDTO;
import com.application.adminservice.dto.BusScheduleDto;
import com.application.adminservice.entity.Bus;
import com.application.adminservice.entity.Schedule;
import com.application.adminservice.repository.BusRepository;
import com.application.adminservice.repository.ScheduleRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusService {
    @Autowired
    private BusRepository busRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;

    private static final String BUS_SERVICE = "busService";

    @CircuitBreaker(name = BUS_SERVICE, fallbackMethod = "fallbackSaveBus")
    public Bus saveBus(Bus bus) {
        if (bus.getBusNumber() == null) {
            throw new RuntimeException("Bus number is required");
        }
        return busRepository.save(bus);
    }

    @CircuitBreaker(name = BUS_SERVICE, fallbackMethod = "fallbackGetAllBuses")
    public Page<Bus> getAllBuses(int page, int size, String busNumber) {
        Pageable pageable = PageRequest.of(page, size);
        if (busNumber != null && !busNumber.isEmpty()) {
            return busRepository.findByBusNumberContainingIgnoreCase(busNumber, pageable);
        }
        return busRepository.findAll(pageable);
    }

    @CircuitBreaker(name = BUS_SERVICE, fallbackMethod = "fallbackGetAllBuse")
    public List<Bus> getAllBuse() {
        return busRepository.findAll();
    }

    @CircuitBreaker(name = BUS_SERVICE, fallbackMethod = "fallbackSearchBuses")
    public List<BusScheduleDto> searchBuses(String origin, String destination, LocalDate date) {
        System.out.println("Searching for origin: " + origin + ", destination: " + destination + ", date: " + date);
        List<Schedule> schedules = scheduleRepository.findByRouteOriginAndRouteDestinationAndDate(origin, destination, date);
        return schedules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private BusScheduleDto convertToDTO(Schedule schedule) {
        BusScheduleDto dto = new BusScheduleDto();
        dto.setBusNumber(schedule.getBus().getBusNumber());
        dto.setOrigin(schedule.getRoute().getOrigin());
        dto.setDestination(schedule.getRoute().getDestination());
        dto.setBusType(schedule.getBus().getBusType());
        dto.setDepartureTime(schedule.getDepartureTime());
        dto.setArrivalTime(schedule.getArrivalTime());
        dto.setCapacity(schedule.getBus().getCapacity());
        dto.setFare(schedule.getFare());
        int totalSeats = schedule.getBus().getCapacity();
        int bookedSeats = scheduleRepository.countByScheduleId(schedule.getScheduleId());
        int availableSeats = totalSeats - bookedSeats;
        dto.setAvailableSeats(availableSeats);
        return dto;
    }

    @CircuitBreaker(name = BUS_SERVICE, fallbackMethod = "fallbackGetBusByNumber")
    public BusDTO getBusByNumber(String busNumber) {
        // Fetch the bus
        Bus bus = busRepository.findByBusNumber(busNumber)
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        // Fetch schedules for the bus
        List<Schedule> schedules = scheduleRepository.findSchedulesByBusNumber(busNumber);

        // Map to DTO
        BusDTO busDTO = new BusDTO();
        busDTO.setBusNumber(bus.getBusNumber());
        busDTO.setBusType(bus.getBusType());
        busDTO.setCapacity(bus.getCapacity());
        List<BusScheduleDto> scheduleDTOs = schedules.stream()
                .map(schedule -> {
                    BusScheduleDto scheduleDTO = new BusScheduleDto();
                    scheduleDTO.setScheduleId(schedule.getScheduleId());
                    scheduleDTO.setDepartureTime(schedule.getDepartureTime());
                    scheduleDTO.setArrivalTime(schedule.getArrivalTime());
                    scheduleDTO.setOrigin(schedule.getRoute().getOrigin());
                    scheduleDTO.setDestination(schedule.getRoute().getDestination());
                    scheduleDTO.setFare(schedule.getFare());
                    int totalSeats = bus.getCapacity();
                    int bookedSeats = scheduleRepository.countByScheduleId(schedule.getScheduleId());
                    int availableSeats = totalSeats - bookedSeats;
                    scheduleDTO.setAvailableSeats(availableSeats);
                    return scheduleDTO;
                })
                .collect(Collectors.toList());

        busDTO.setSchedules(scheduleDTOs);
        return busDTO;
    }

    public Bus fallbackSaveBus(Bus bus, Throwable t) {
        System.err.println("Fallback for saveBus: " + t.getMessage());
        return new Bus();
    }

    public Page<Bus> fallbackGetAllBuses(int page, int size, String busNumber, Throwable t) {
        System.err.println("Fallback for getAllBuses: " + t.getMessage());
        return Page.empty();
    }

    public List<Bus> fallbackGetAllBuse(Throwable t) {
        System.err.println("Fallback for getAllBuse: " + t.getMessage());
        return Collections.emptyList(); // Returning empty list
    }

    public List<BusScheduleDto> fallbackSearchBuses(String origin, String destination, LocalDate date, Throwable t) {
        System.err.println("Fallback for searchBuses: " + t.getMessage());
        return Collections.emptyList(); // Returning empty list
    }

    public BusDTO fallbackGetBusByNumber(String busNumber, Throwable t) {
        System.err.println("Fallback for getBusByNumber: " + t.getMessage());
        BusDTO fallbackBusDTO = new BusDTO();
        fallbackBusDTO.setBusNumber("N/A");
        fallbackBusDTO.setBusType("Unknown");
        fallbackBusDTO.setCapacity(0);
        fallbackBusDTO.setSchedules(Collections.emptyList());
        return fallbackBusDTO;
    }

}