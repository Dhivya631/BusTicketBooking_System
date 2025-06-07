package com.application.adminservice.controller;

import com.application.adminservice.entity.Route;
import com.application.adminservice.repository.RouteRepository;
import com.application.adminservice.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/admin/routes")
public class RouteController {
    @Autowired
    private RouteService routeService;
    @Autowired
    private RouteRepository routeRepository;

    @GetMapping("/view")
    public String viewRoutes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            Model model) {
        // Define page size
        int pageSize = 5;

        // Fetch routes based on search criteria and pagination
        Page<Route> routePage = routeService.findRoutesByOriginAndDestination(origin, destination, PageRequest.of(page - 1, pageSize));
        List<Route> routes = routePage.getContent();

        // Add attributes to the model
        model.addAttribute("routes", routes);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", routePage.getTotalPages());
        model.addAttribute("origin", origin);
        model.addAttribute("destination", destination);
        return "routes";
    }
    @GetMapping("/add")
    public String addRouteForm(Model model) {
        model.addAttribute("route", new Route());
        return "add-route";
    }

    @PostMapping("/add")
    public String saveRoute(@ModelAttribute Route route) {
        routeRepository.save(route);
        return "redirect:/api/admin/routes/view";
    }

    @GetMapping("/edit/{id}")
    public String editRouteForm(@PathVariable Long id, Model model) {
        Route route = routeRepository.findById(id).orElseThrow(() -> new RuntimeException("Route not found"));
        model.addAttribute("route", route);
        return "edit-route";
    }

    @PostMapping("/edit/{id}")
    public String updateRoute(@PathVariable Long id, @ModelAttribute Route route) {
        route.setRouteId(id);
        routeRepository.save(route);
        return "redirect:/api/admin/routes/view";
    }

    @GetMapping("/delete/{id}")
    public String deleteRoute(@PathVariable Long id) {
        routeRepository.deleteById(id);
        return "redirect:/api/admin/routes/view";
    }
}