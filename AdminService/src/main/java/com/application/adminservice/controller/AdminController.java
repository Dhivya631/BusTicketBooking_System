package com.application.adminservice.controller;

import com.application.adminservice.dto.BookingDTO;
import com.application.adminservice.entity.User;
import com.application.adminservice.service.AdminService;
import com.application.adminservice.service.BusService;
import com.application.adminservice.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/admin")
public class AdminController {
    private static final Logger logger= LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;
    @Autowired
    private BusService busService;
    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/login")
    public String adminPage(){
        return "adminLogin";
    }

    @GetMapping("/dash")
    public String adminDashboard(){
        return "adminDashboard";
    }

    @GetMapping("/homePage")
    public String home(){
        return "home";
    }


    @PostMapping("/login")
    public String adminLogin(@RequestParam("username") String username, @RequestParam("password") String password, Model model){
        logger.info("Given username: "+username+", password: "+password);
        if(adminService.authenticateLogin(username,password)){
            model.addAttribute("successMessage","Admin login successfully");
            return "adminDashboard";
        }
        model.addAttribute("errorMessage","Invalid username or password");
        return "adminLogin";
    }
    @GetMapping("/users")
    public String getAllUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            Model model) {

        // Fetch paginated users from the UserService API
        Page<User> userPage = adminService.getAllUserFromUserService(page, size,search);

        model.addAttribute("users", userPage.getContent()); // List of users for the current page
        model.addAttribute("currentPage", page); // Current page number
        model.addAttribute("totalPages", userPage.getTotalPages()); // Total number of pages
        model.addAttribute("search", search);
        return "viewUser";
    }
    @GetMapping("/booking-history")
    public String viewBooking(Model model){
        List<BookingDTO> booking=adminService.getAllBooking();
        model.addAttribute("bookings",booking);
        return "bookingHistory";
    }
    @PostMapping("/booking/{id}/cancel")
    public String cancelBooking(@PathVariable Long id,@RequestParam("redirecturl") String redirecturl){
        adminService.cancelBooking(id);
        return "redirect:"+redirecturl;
    }
}
