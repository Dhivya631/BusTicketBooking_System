package com.application.adminservice.controller;

import com.application.adminservice.dto.BusDTO;
import com.application.adminservice.dto.BusScheduleDto;
import com.application.adminservice.entity.Bus;
import com.application.adminservice.repository.BusRepository;
import com.application.adminservice.service.BusService;
import com.application.adminservice.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/api/admin")
public class BusController {

    private static final Logger logger= LoggerFactory.getLogger(BusController.class);

    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private BusService busService;
    @Autowired
    private BusRepository busRepository;
    // bus
    @GetMapping("/addBus")
    public String addbus(Model model){
        model.addAttribute("bus",new Bus());
        return "addBus";
    }

    @PostMapping("/addBus")
    public String bus(@ModelAttribute Bus bus, RedirectAttributes redirectAttributes){
        busService.saveBus(bus);
        redirectAttributes.addFlashAttribute("successMessage","Added bus successfully");
        return "redirect:/api/admin/addBus";
    }
    @GetMapping("/viewBuses")
    public String viewBuses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String busNumber,
            Model model) {

        Page<Bus> busPage = busService.getAllBuses(page, size, busNumber);
        model.addAttribute("buses", busPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", busPage.getTotalPages());
        model.addAttribute("busNumber", busNumber); // Retain search input
        return "viewBus";
    }

    @GetMapping("/search")
    public ResponseEntity<List<BusScheduleDto>> searchSchedules(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso= DateTimeFormat.ISO.DATE) LocalDate date) {
        List<BusScheduleDto> bus=busService.searchBuses(origin,destination,date);
        return ResponseEntity.ok(bus);
    }
    @GetMapping("/edit/{id}")
    public String editBusForm(@PathVariable Long id, Model model) {
        Bus bus = busRepository.findById(id).orElseThrow(() -> new RuntimeException("Bus not found"));
        model.addAttribute("buses", bus);
        return "edit-bus";
    }

    @PostMapping("/edit/{id}")
    public String updateBus(@PathVariable Long id, @ModelAttribute Bus bus) {
        bus.setBusId(id);
        busRepository.save(bus);
        return "redirect:/api/admin/viewBuses";
    }

    @GetMapping("/delete/{id}")
    public String deleteBus(@PathVariable Long id) {
        busRepository.deleteById(id);
        return "redirect:/api/admin/viewBuses";
    }
    @GetMapping("/buses/{busNumber}")
    public ResponseEntity<BusDTO> getBusByNumber(@PathVariable String busNumber) {
        BusDTO bus=busService.getBusByNumber(busNumber);
        return ResponseEntity.ok(bus);
    }

}