package com.example.library_management_system.controller;

import com.example.library_management_system.dto.auth.AuthResponseDTO;
import com.example.library_management_system.dto.auth.LoginRequestDTO;
import com.example.library_management_system.dto.auth.RegisterRequestDTO;
import com.example.library_management_system.exception.DuplicateResourceException;
import com.example.library_management_system.exception.UnauthorizedException;
import com.example.library_management_system.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController Tests")
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AuthService authService;

  @MockBean
  private com.example.library_management_system.security.JwtUtil jwtUtil;

  private RegisterRequestDTO registerRequestDTO;
  private LoginRequestDTO loginRequestDTO;
  private AuthResponseDTO authResponseDTO;

  @BeforeEach
  void setUp() {
    registerRequestDTO = new RegisterRequestDTO();
    registerRequestDTO.setUsername("testuser");
    registerRequestDTO.setPassword("password123");
    registerRequestDTO.setRole("MEMBER");

    loginRequestDTO = new LoginRequestDTO();
    loginRequestDTO.setUsername("testuser");
    loginRequestDTO.setPassword("password123");

    authResponseDTO = new AuthResponseDTO();
    authResponseDTO.setUsername("testuser");
    authResponseDTO.setRole("MEMBER");
    authResponseDTO.setToken("jwt-token-12345");
  }

  @Test
  @DisplayName("register - Valid Request - Returns 201")
  void register_ValidRequest_Returns201() throws Exception {
    // Arrange
    when(authService.register(any(RegisterRequestDTO.class))).thenReturn(authResponseDTO);

    // Act & Assert
    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequestDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username", is("testuser")))
        .andExpect(jsonPath("$.role", is("MEMBER")))
        .andExpect(jsonPath("$.token", is("jwt-token-12345")));
  }

  @Test
  @DisplayName("register - Invalid Request - Returns 400")
  void register_InvalidRequest_Returns400() throws Exception {
    // Arrange - Create invalid request (missing required fields)
    RegisterRequestDTO invalidRequest = new RegisterRequestDTO();
    // Missing username and password

    // Act & Assert
    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("register - Duplicate Username - Returns 409")
  void register_DuplicateUsername_Returns409() throws Exception {
    // Arrange
    when(authService.register(any(RegisterRequestDTO.class)))
        .thenThrow(new DuplicateResourceException("Username already exists: testuser"));

    // Act & Assert
    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequestDTO)))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("register - Short Password - Returns 400")
  void register_ShortPassword_Returns400() throws Exception {
    // Arrange
    registerRequestDTO.setPassword("123"); // Too short (< 6 characters)

    // Act & Assert
    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequestDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("login - Valid Credentials - Returns 200")
  void login_ValidCredentials_Returns200() throws Exception {
    // Arrange
    when(authService.login(any(LoginRequestDTO.class))).thenReturn(authResponseDTO);

    // Act & Assert
    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequestDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username", is("testuser")))
        .andExpect(jsonPath("$.role", is("MEMBER")))
        .andExpect(jsonPath("$.token", is("jwt-token-12345")));
  }

  @Test
  @DisplayName("login - Invalid Credentials - Returns 401")
  void login_InvalidCredentials_Returns401() throws Exception {
    // Arrange
    when(authService.login(any(LoginRequestDTO.class)))
        .thenThrow(new UnauthorizedException("Invalid username or password"));

    // Act & Assert
    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequestDTO)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("login - Invalid Request - Returns 400")
  void login_InvalidRequest_Returns400() throws Exception {
    // Arrange - Create invalid request (missing required fields)
    LoginRequestDTO invalidRequest = new LoginRequestDTO();
    // Missing username and password

    // Act & Assert
    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }
}
