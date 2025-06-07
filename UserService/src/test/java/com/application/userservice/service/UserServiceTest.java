package com.application.userservice.service;

import com.application.userservice.configuration.JwtTokenProvider;
import com.application.userservice.dto.UserDTO;
import com.application.userservice.entity.User;
import com.application.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @InjectMocks
    private UserService userService;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPhoneno(1234567890L);
    }

    @Test
    @DisplayName("Authenticated user successfully")
    void testAuthenticateUser_Success() {
        String username = "john";
        String password = "password";
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("mocked-jwt-token");

        String token = userService.authenticateUser(username, password);

        assertNotNull(token);
        assertEquals("mocked-jwt-token", token);
    }

    @Test
    @DisplayName("Fallback for authenticated user")
    void testAuthenticateUser_Fallback() {
        String fallbackResponse = userService.fallbackAuthenticateUser("john", "password", new RuntimeException());

        // Assert
        assertEquals("Authentication service is currently unavailable. Please try again later.", fallbackResponse);
    }

    @Test
    @DisplayName("Get all users")
    void testGetAllUsers_WithSearch() {
        // Arrange
        List<User> users = Arrays.asList(user);
        Page<User> userPage = new PageImpl<>(users);
        when(userRepository.findByNameContaining("John", PageRequest.of(0, 5))).thenReturn(userPage);

        // Act
        Page<User> result = userService.getAllUser(0, 5, "John");

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Get all users without search")
    void testGetAllUsers_WithoutSearch() {
        List<User> users = Arrays.asList(user);
        Page<User> userPage = new PageImpl<>(users);
        when(userRepository.findAll(PageRequest.of(0, 5))).thenReturn(userPage);

        // Act
        Page<User> result = userService.getAllUser(0, 5, "");

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Fallback for get all user")
    void testGetAllUsers_Fallback() {
        // Act
        Page<User> fallbackResult = userService.fallbackGetAllUsers(0, 5, "John", new RuntimeException());

        // Assert
        assertTrue(fallbackResult.isEmpty());
    }

    @Test
    @DisplayName("Get user details using email successfully")
    void testGetUserByEmail_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // Act
        UserDTO userDTO = userService.getUserByEmail("john@example.com");

        // Assert
        assertNotNull(userDTO);
        assertEquals("John Doe", userDTO.getName());
    }

    @Test
    @DisplayName("Fallback for get user details using email")
    void testGetUserByEmail_Fallback() {
        // Act
        UserDTO fallbackUser = userService.fallbackGetUserByEmail("test@example.com", new RuntimeException());

        // Assert
        assertEquals("unknown", fallbackUser.getName());
        assertEquals("unknown@gmail.com", fallbackUser.getEmail());
    }

    @Test
    @DisplayName("Update user details successfully")
    void testUpdateUserDetails_Success() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User updatedUser = userService.updateUserDetails("john", "John Updated", "johnupdated@example.com", 9876543210L);

        // Assert
        assertEquals("John Updated", updatedUser.getName());
        assertEquals("johnupdated@example.com", updatedUser.getEmail());
        assertEquals(9876543210L, updatedUser.getPhoneno());
    }

    @Test
    @DisplayName("Fallback for update user details")
    void testUpdateUserDetails_Fallback() {
        // Act
        User fallbackUser = userService.fallbackUpdateUserDetails("john", "John Updated", "johnupdated@example.com", 9876543210L, new RuntimeException());

        // Assert
        assertEquals("user", fallbackUser.getName());
        assertEquals("user@gmail.com", fallbackUser.getEmail());
    }

    @Test
    @DisplayName("Find user details using username successfully")
    void testFindUserByUsername_Success() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        // Act
        User foundUser = userService.findUserByUsername("john");

        // Assert
        assertNotNull(foundUser);
        assertEquals("John Doe", foundUser.getName());
    }

    @Test
    @DisplayName("Fallback for finding user details using fallback")
    void testFindUserByUsername_Fallback() {
        // Act
        User fallbackUser = userService.fallbackFindUserByUsername("john", new RuntimeException());

        // Assert
        assertEquals("default", fallbackUser.getName());
        assertEquals("default@gmail.com", fallbackUser.getEmail());
    }
}
