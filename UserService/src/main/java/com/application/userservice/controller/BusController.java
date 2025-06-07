package com.application.userservice.controller;

import com.application.userservice.dto.BusScheduleDTO;
import com.application.userservice.service.BusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/api/buses")
public class BusController {
    @Autowired
    private BusService busService;

    @GetMapping("/search-buses")
    public String searchBuses(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        if (origin != null && destination != null && date != null) {
            List<BusScheduleDTO> buses = busService.searchBuses(origin, destination, date);
            model.addAttribute("buses", buses);
        }
        return "search-buses";
    }
}