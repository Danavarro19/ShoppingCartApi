package com.org.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.authservice.dto.AuthResponse;
import com.org.authservice.dto.LoginRequest;
import com.org.authservice.dto.RegisterRequest;
import com.org.authservice.dto.UserInfoResponse;
import com.org.authservice.model.Role;
import com.org.authservice.model.User;
import com.org.authservice.repository.UserRepository;
import com.org.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Clean up the database before each test
        userRepository.deleteAll();
    }

    @Test
    void testRegisterUser() throws Exception {
        // Create a register request
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Perform the register request
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        // Verify the user was created in the database
        User user = userRepository.findByEmail("test@example.com").orElse(null);
        assertNotNull(user);
    }

    @Test
    void testRegisterUserWithExistingEmail() throws Exception {
        // Create a user in the database
        User user = User.builder()
                .email("existing@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // Create a register request with the same email
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        // Perform the register request and expect a 400 Bad Request
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Email already registered")));
    }

    @Test
    void testLogin() throws Exception {
        // Create a user in the database
        User user = User.builder()
                .email("login@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // Create a login request
        LoginRequest request = new LoginRequest();
        request.setEmail("login@example.com");
        request.setPassword("password123");

        // Perform the login request
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        // Create a user in the database
        User user = User.builder()
                .email("login@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // Create a login request with wrong password
        LoginRequest request = new LoginRequest();
        request.setEmail("login@example.com");
        request.setPassword("wrongpassword");

        // Perform the login request and expect a 401 Unauthorized
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Invalid email or password")));
    }

    @Test
    void testGetCurrentUser() throws Exception {
        // Create a user in the database
        User user = User.builder()
                .email("me@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // Login to get a token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("me@example.com");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract the token from the response
        String responseJson = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseJson, AuthResponse.class);
        String token = authResponse.getToken();

        // Use the token to access the /me endpoint
        mockMvc.perform(get("/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("me@example.com")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    void testGetCurrentUserWithoutToken() throws Exception {
        // Try to access the /me endpoint without a token
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}