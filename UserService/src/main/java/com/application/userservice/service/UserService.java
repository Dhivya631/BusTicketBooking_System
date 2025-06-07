package com.application.userservice.service;

import com.application.userservice.configuration.JwtTokenProvider;
import com.application.userservice.dto.UserDTO;
import com.application.userservice.entity.User;
import com.application.userservice.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final String USER_SERVICE = "userService";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @CircuitBreaker(name = USER_SERVICE, fallbackMethod = "fallbackAuthenticateUser")
    public String authenticateUser(String username,String password){
        Authentication authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtTokenProvider.generateToken(authentication);
    }
    @CircuitBreaker(name = USER_SERVICE, fallbackMethod = "fallbackGetAllUsers")
    public Page<User> getAllUser(int page, int size, String search) {
        if (search != null && !search.isEmpty()) {
            return userRepository.findByNameContaining(search, PageRequest.of(page, size));
        } else {
            return userRepository.findAll(PageRequest.of(page, size));
        }
    }
    @CircuitBreaker(name = USER_SERVICE, fallbackMethod = "fallbackGetUserByEmail")
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return convertToDTO(user);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }
    @CircuitBreaker(name = USER_SERVICE, fallbackMethod = "fallbackUpdateUserDetails")
    public User updateUserDetails(String username, String name, String email, Long phoneno) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        user.setName(name);
        user.setEmail(email);
        user.setPhoneno(phoneno);

        return userRepository.save(user);
    }

    @CircuitBreaker(name = USER_SERVICE, fallbackMethod = "fallbackFindUserByUsername")
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }
    public String fallbackAuthenticateUser(String username, String password, Throwable t) {
        return "Authentication service is currently unavailable. Please try again later.";
    }

    public Page<User> fallbackGetAllUsers(int page, int size, String search, Throwable t) {
        return Page.empty();
    }

    public UserDTO fallbackGetUserByEmail(String email, Throwable t) {
        UserDTO user=new UserDTO();
        user.setUserId(0L);
        user.setName("unknown");
        user.setEmail("unknown@gmail.com");
        return user;
    }

    public User fallbackUpdateUserDetails(String username, String name, String email, Long phoneno, Throwable t) {
        User user=new User();
        user.setId(0L);
        user.setName("user");
        user.setEmail("user@gmail.com");
        user.setPhoneno(9810291892L);
        return user;
    }

    public User fallbackFindUserByUsername(String username, Throwable t) {
        User user=new User();
        user.setId(0L);
        user.setName("default");
        user.setEmail("default@gmail.com");
        return user;
    }
}
