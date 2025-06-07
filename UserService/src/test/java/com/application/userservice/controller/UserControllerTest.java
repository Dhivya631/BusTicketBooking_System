package com.application.userservice.controller;

import com.application.userservice.configuration.JwtTokenProvider;
import com.application.userservice.dto.UserDTO;
import com.application.userservice.entity.AuthRequest;
import com.application.userservice.entity.User;
import com.application.userservice.repository.UserRepository;
import com.application.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userservice;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Customer dashboard")
    void testCustomerDash() throws Exception {
        mockMvc.perform(get("/api/customer/dash"))
                .andExpect(status().isOk())
                .andExpect(view().name("customerDashboard"));
    }

    @Test
    @DisplayName("Display home page")
    void testCustomerHomePage() throws Exception {
        mockMvc.perform(get("/api/customer/homePage"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    @DisplayName("View Registration page for customer")
    void testViewRegisterPage() throws Exception {
        mockMvc.perform(get("/api/customer/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("customerRegistration"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("Customer registered successfully")
    void testRegisterUser_Success() throws Exception {
        User user = new User();
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPhoneno(1234567890L);
        user.setPassword("password");

        Mockito.when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByPhoneno(user.getPhoneno())).thenReturn(Optional.empty());
        Mockito.when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");

        mockMvc.perform(post("/api/customer/register")
                        .param("username", user.getUsername())
                        .param("email", user.getEmail())
                        .param("phoneno", String.valueOf(user.getPhoneno()))
                        .param("password", user.getPassword()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/api/customer/login"));
    }

    @Test
    @DisplayName("Username exists")
    void testRegisterUser_UsernameExists() throws Exception {
        User user = new User();
        user.setUsername("testUser");

        Mockito.when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/customer/register")
                        .param("username", user.getUsername()))
                .andExpect(status().isOk())
                .andExpect(view().name("customerRegistration"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Display login page")
    void testShowLoginPage() throws Exception {
        mockMvc.perform(get("/api/customer/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("customerLogin"));
    }

    @Test
    @DisplayName("Authenticated customer successfully")
    void testAuthenticateUser_Success() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("testUser");
        authRequest.setPassword("password");
        String token = "jwtToken";

        Mockito.when(authenticationManager.authenticate(Mockito.any()))
                .thenReturn(Mockito.mock(Authentication.class));
        Mockito.when(userservice.authenticateUser(authRequest.getUsername(), authRequest.getPassword()))
                .thenReturn(token);

        mockMvc.perform(post("/api/customer/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));
    }

    @Test
    @DisplayName("Authenticated customer - unsuccessful")
    void testAuthenticateUser_Failure() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("wrongUser");
        authRequest.setPassword("wrongPassword");

        Mockito.when(authenticationManager.authenticate(Mockito.any()))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/customer/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.sessionId").value("Invalid username or password"));
    }

    @Test
    @DisplayName("View all users")
    void testViewAllUsers() throws Exception {
        List<User> userList = new ArrayList<>();
        User user=new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        userList.add(user);

        Page<User> userPage = new PageImpl<>(userList);
        Mockito.when(userservice.getAllUser(Mockito.anyInt(), Mockito.anyInt(), Mockito.any()))
                .thenReturn(userPage);

        mockMvc.perform(get("/api/customer/viewAllUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Get user deatils using id")
    void testGetUserById() throws Exception {
        User user=new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    @DisplayName("Get user details using email")
    void testGetUserByEmail() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(1L);
        userDTO.setEmail("test@example.com");

        Mockito.when(userservice.getUserByEmail("test@example.com")).thenReturn(userDTO);

        mockMvc.perform(get("/api/customer/email")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Update password successfully")
    void testUpdatePassword_Success() throws Exception {
        User user=new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");

        Mockito.when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("oldPassword", user.getPassword())).thenReturn(true);
        Mockito.when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        mockMvc.perform(post("/api/customer/update-password")
                        .sessionAttr("username", "testUser")
                        .param("currentPassword", "oldPassword")
                        .param("newPassword", "newPassword")
                        .param("confirmPassword", "newPassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-password"))
                .andExpect(model().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("Update password - Incorrect current password")
    void testUpdatePassword_IncorrectCurrentPassword() throws Exception {
        User user=new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPassword("encode");

        Mockito.when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

        mockMvc.perform(post("/api/customer/update-password")
                        .sessionAttr("username", "testUser")
                        .param("currentPassword", "wrongPassword")
                        .param("newPassword", "newPassword")
                        .param("confirmPassword", "newPassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-password"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Update user details")
    void testUpdateUserDetails() throws Exception {
        User user=new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        User updatedUser = new User();
        updatedUser.setUsername("testUser");
        updatedUser.setEmail("new@example.com");
        updatedUser.setPhoneno(9876543210L);
        updatedUser.setPassword("password");

        Mockito.when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        Mockito.when(userservice.updateUserDetails("testUser", "newUser", "new@example.com", 9876543210L))
                .thenReturn(updatedUser);

        mockMvc.perform(post("/api/customer/update-details")
                        .sessionAttr("username", "testUser")
                        .param("name", "newUser")
                        .param("email", "new@example.com")
                        .param("phoneno", String.valueOf(9876543210L)))
                .andExpect(status().isOk())
                .andExpect(view().name("update-user"))
                .andExpect(model().attributeExists("successMessage"));
    }

}