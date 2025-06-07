package com.application.userservice.service;

import com.application.userservice.entity.User;
import com.application.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(()-> new RuntimeException("User not found with username: " + username));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),  List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase())));
    }
}