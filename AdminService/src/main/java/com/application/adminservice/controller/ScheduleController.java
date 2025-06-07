package com.application.adminservice.controller;

import com.application.adminservice.dto.BusScheduleDto;
import com.application.adminservice.entity.Bus;
import com.application.adminservice.entity.Route;
import com.application.adminservice.entity.Schedule;
import com.application.adminservice.repository.RouteRepository;
import com.application.adminservice.repository.ScheduleRepository;
import com.application.adminservice.service.BusService;
import com.application.adminservice.service.RouteService;
import com.application.adminservice.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/admin/schedules")
public class ScheduleController {
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private RouteService routeService;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private BusService busService;
    @Autowired
    private RouteRepository routeRepository;

    // View all schedules with pagination and search
    @GetMapping("/view")
    public String viewSchedules(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            Model model) {
        // Define page size
        int pageSize = 5;

        // Fetch schedules based on search criteria and pagination
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Schedule> schedulePage;

        if (origin != null && destination != null) {
            schedulePage = scheduleRepository.findByRoute_OriginContainingAndRoute_DestinationContaining(origin, destination, pageable);
        } else if (origin != null) {
            schedulePage = scheduleRepository.findByRoute_OriginContaining(origin, pageable);
        } else if (destination != null) {
            schedulePage = scheduleRepository.findByRoute_DestinationContaining(destination, pageable);
        } else {
            schedulePage = scheduleRepository.findAll(pageable);
        }

        List<Schedule> schedules = schedulePage.getContent();

        // Add attributes to the model
        model.addAttribute("schedules", schedules);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", schedulePage.getTotalPages());
        model.addAttribute("origin", origin);
        model.addAttribute("destination", destination);
        return "schedules";
    }

    // Display form to add a new schedule
    @GetMapping("/add")
    public String showAddScheduleForm(Model model) {
        List<Route> routes = routeService.getAllRoutes(); // Fetch routes from service
        List<Bus> buses = busService.getAllBuse(); // Fetch buses from service

        model.addAttribute("routes", routes);
        model.addAttribute("buses", buses);
        model.addAttribute("schedule", new Schedule());
        return "add-schedule";
    }
    // Save a new schedule
    @PostMapping("/add")
    public String saveSchedule(@ModelAttribute Schedule schedule) {
        scheduleRepository.save(schedule);
        return "redirect:/api/admin/schedules/view";
    }

    // Display form to edit a schedule
    @GetMapping("/edit/{id}")
    public String editScheduleForm(@PathVariable Long id, Model model) {
        List<Route> routes = routeService.getAllRoutes(); // Fetch routes from service
        List<Bus> buses = busService.getAllBuse(); // Fetch buses from service

        model.addAttribute("routes", routes);
        model.addAttribute("buses", buses);
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        model.addAttribute("schedule", schedule);
        return "edit-schedule";
    }

    // Update a schedule
    @PostMapping("/edit/{id}")
    public String updateSchedule(@PathVariable Long id, @ModelAttribute Schedule schedule) {
        schedule.setScheduleId(id);
        scheduleRepository.save(schedule);
        return "redirect:/api/admin/schedules/view";
    }

    // Delete a schedule
    @GetMapping("/delete/{id}")
    public String deleteSchedule(@PathVariable Long id) {
        scheduleRepository.deleteById(id);
        return "redirect:/api/admin/schedules/view";
    }
    @PutMapping("/{scheduleId}/update-available-seats")
    public ResponseEntity<String> updateAvailableSeats(
            @PathVariable Long scheduleId,
            @RequestParam int availableSeats) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        schedule.setAvailableSeats(availableSeats);
        scheduleRepository.save(schedule);

        return ResponseEntity.ok("Available seats updated successfully");
    }
    @GetMapping("/{scheduleId}")
    public ResponseEntity<BusScheduleDto> getScheduleById(@PathVariable Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        BusScheduleDto scheduleDTO = new BusScheduleDto();
        scheduleDTO.setScheduleId(schedule.getScheduleId());
        scheduleDTO.setBusNumber(schedule.getBus().getBusNumber());
        scheduleDTO.setBusType(schedule.getBus().getBusType());
        scheduleDTO.setOrigin(schedule.getRoute().getOrigin());
        scheduleDTO.setDestination(schedule.getRoute().getDestination());
        scheduleDTO.setArrivalTime(schedule.getArrivalTime());
        scheduleDTO.setDepartureTime(schedule.getDepartureTime());
        scheduleDTO.setCapacity(schedule.getBus().getCapacity());
        scheduleDTO.setAvailableSeats(schedule.getAvailableSeats());
        scheduleDTO.setFare(schedule.getFare());

        return ResponseEntity.ok(scheduleDTO);
    }

}
