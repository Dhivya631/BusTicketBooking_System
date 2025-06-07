package com.application.userservice.controller;

import com.application.userservice.dto.UserDTO;
import com.application.userservice.entity.AuthRequest;
import com.application.userservice.entity.AuthResponse;
import com.application.userservice.entity.User;
import com.application.userservice.repository.UserRepository;
import com.application.userservice.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/customer")
public class UserController {
    private static final Logger logger= LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userservice;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/dash")
    public String customerDash(){
        return "customerDashboard";
    }

    @GetMapping("/homePage")
    public String customer(){
        return "home";
    }


    @GetMapping("/register")
    public String viewRegisterPage(Model model){
        model.addAttribute("user", new User());
        return "customerRegistration";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("errorMessage", "Username already exists.");
            return "customerRegistration";
        }
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            model.addAttribute("errorMessage", "Email already exists");
            return "customerRegistration";
        }
        if(userRepository.findByPhoneno(user.getPhoneno()).isPresent()){
            model.addAttribute("errorMessage","PhoneNo already exists");
            return "customerRegistration";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        model.addAttribute("successMessage","User registered successfully");
        return "redirect:/api/customer/login";
    }

    @GetMapping("/login")
    public String showUserPage(){
        return "customerLogin";
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest authRequest, HttpSession session) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = userservice.authenticateUser(authRequest.getUsername(), authRequest.getPassword());
            session.setAttribute("username",authRequest.getUsername());
            session.setAttribute("password",authRequest.getPassword());
            return ResponseEntity.ok(new AuthResponse(token,session.getId()));
        }
        catch (AuthenticationException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null,"Invalid username or password"));
        }
    }

    @GetMapping("/logout")
    public String showLoginPage(@RequestParam(value="logout",required = false) String logout, Model model) {
        if("true".equals(logout)){
            model.addAttribute("logoutMessage","Logged out successfully");
        }
        return "customerLogin";
    }
    @GetMapping("/viewAllUser")
    public ResponseEntity<Page<User>> viewAllUser(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(required = false) String search, Model model) {

        Page<User> userPage = userservice.getAllUser(page,size,search);
        model.addAttribute("users",userPage);
        return ResponseEntity.ok(userPage);
    }
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getScheduleById(@PathVariable Long userId) {
        User user=userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhoneno(user.getPhoneno());
        return ResponseEntity.ok(userDTO);
    }
    @GetMapping("/email")
    public ResponseEntity<UserDTO> getUserByEmail(@RequestParam String email){
        UserDTO user=userservice.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }
    @GetMapping("/update-password")
    public String showUpdatePasswordPage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/api/customer/login";
        }
        model.addAttribute("username", username);
        return "update-password";
    }
    @PostMapping("/update-password")
    public String updatePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/api/customer/login";
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("errorMessage", "Current password is incorrect.");
            return "update-password";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "New password and confirm password do not match.");
            return "update-password";
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        model.addAttribute("successMessage", "Password updated successfully!");
        return "update-password";
    }
    @GetMapping("/update-details")
    public String showUpdatePage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/api/customer/login";
        }
        User user = userservice.findUserByUsername(username);
        model.addAttribute("user", user);
        return "update-user";
    }
    @PostMapping("/update-details")
    public String updateUserDetails(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam Long phoneno,HttpSession session,
            Model model) {
        try {
            String username = (String) session.getAttribute("username");
            if (username == null) {
                return "redirect:/api/customer/login";
            }
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            User updatedUser = userservice.updateUserDetails(user.getUsername(), name, email, phoneno);
            model.addAttribute("successMessage", "User details updated successfully!");
            model.addAttribute("user", updatedUser);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "update-user";
    }
}
