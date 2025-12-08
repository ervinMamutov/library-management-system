package com.example.library_management_system.controller;

import com.example.library_management_system.dto.user.PasswordChangeRequestDTO;
import com.example.library_management_system.dto.user.UserResponseDTO;
import com.example.library_management_system.dto.user.UserUpdateRequestDTO;
import com.example.library_management_system.exception.ResourceNotFoundException;
import com.example.library_management_system.exception.UnauthorizedException;
import com.example.library_management_system.model.Role;
import com.example.library_management_system.model.User;
import com.example.library_management_system.repository.UserRepository;
import com.example.library_management_system.security.JwtAuthenticationFilter;
import com.example.library_management_system.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController Tests")
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private UserService userService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockBean
  private com.example.library_management_system.security.JwtUtil jwtUtil;

  private UserResponseDTO userResponseDTO;
  private UserUpdateRequestDTO updateRequestDTO;

  @BeforeEach
  void setUp() {
    userResponseDTO = new UserResponseDTO();
    userResponseDTO.setId(1L);
    userResponseDTO.setUsername("testuser");
    userResponseDTO.setRole("MEMBER");

    updateRequestDTO = new UserUpdateRequestDTO();
    updateRequestDTO.setUsername("updateduser");
    updateRequestDTO.setRole("LIBRARIAN");
  }

  @Test
  @DisplayName("findAllUsers - As Admin - Returns 200")
  @WithMockUser(roles = "ADMIN")
  void findAllUsers_AsAdmin_Returns200() throws Exception {
    // Arrange
    UserResponseDTO user1 = new UserResponseDTO();
    user1.setId(1L);
    user1.setUsername("user1");
    user1.setRole("MEMBER");

    UserResponseDTO user2 = new UserResponseDTO();
    user2.setId(2L);
    user2.setUsername("user2");
    user2.setRole("LIBRARIAN");

    List<UserResponseDTO> users = Arrays.asList(user1, user2);
    when(userService.findAllUsers()).thenReturn(users);

    // Act & Assert
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].username", is("user1")))
        .andExpect(jsonPath("$[1].username", is("user2")));
  }

  // Security tests moved to UserControllerSecurityTest

  @Test
  @DisplayName("findUserById - As Admin - Returns 200")
  @WithMockUser(roles = "ADMIN")
  void findUserById_AsAdmin_Returns200() throws Exception {
    // Arrange
    when(userService.findUserById(1L)).thenReturn(userResponseDTO);

    // Act & Assert
    mockMvc.perform(get("/api/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.username", is("testuser")))
        .andExpect(jsonPath("$.role", is("MEMBER")));
  }

  // Security test moved to UserControllerSecurityTest

  @Test
  @DisplayName("findUserById - Non-Existing User - Returns 404")
  @WithMockUser(roles = "ADMIN")
  void findUserById_NonExistingUser_Returns404() throws Exception {
    // Arrange
    when(userService.findUserById(999L))
        .thenThrow(new ResourceNotFoundException("User not found with id: 999"));

    // Act & Assert
    mockMvc.perform(get("/api/users/999"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("updateUser - As Admin - Returns 200")
  @WithMockUser(roles = "ADMIN")
  void updateUser_AsAdmin_Returns200() throws Exception {
    // Arrange
    UserResponseDTO updatedUser = new UserResponseDTO();
    updatedUser.setId(1L);
    updatedUser.setUsername("updateduser");
    updatedUser.setRole("LIBRARIAN");

    when(userService.updateUser(eq(1L), any(UserUpdateRequestDTO.class)))
        .thenReturn(updatedUser);

    // Act & Assert
    mockMvc.perform(put("/api/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequestDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.username", is("updateduser")))
        .andExpect(jsonPath("$.role", is("LIBRARIAN")));
  }

  // Security test moved to UserControllerSecurityTest

  @Test
  @DisplayName("updateRole - As Admin - Returns 200")
  @WithMockUser(roles = "ADMIN")
  void updateRole_AsAdmin_Returns200() throws Exception {
    // Arrange
    UserResponseDTO updatedUser = new UserResponseDTO();
    updatedUser.setId(1L);
    updatedUser.setUsername("testuser");
    updatedUser.setRole("ADMIN");

    Map<String, String> roleRequest = new HashMap<>();
    roleRequest.put("role", "ADMIN");

    when(userService.updateRole(1L, "ADMIN")).thenReturn(updatedUser);

    // Act & Assert
    mockMvc.perform(patch("/api/users/1/role")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role", is("ADMIN")));
  }

  // Security test moved to UserControllerSecurityTest

  // Security test moved to UserControllerSecurityTest (requires authentication context)

  @Test
  @DisplayName("changePassword - Other User Password As Member - Returns 401")
  @WithMockUser(username = "member1", roles = "MEMBER")
  void changePassword_OtherUserPasswordAsMember_Returns401() throws Exception {
    // Arrange
    PasswordChangeRequestDTO passwordDTO = new PasswordChangeRequestDTO();
    passwordDTO.setNewPassword("newpassword123");

    User currentUser = new User();
    currentUser.setId(999L); // Different user ID
    currentUser.setUsername("member1");
    currentUser.setRole(Role.MEMBER);

    when(userRepository.findByUsername("member1")).thenReturn(Optional.of(currentUser));
    doThrow(new UnauthorizedException("You can only change your own password"))
        .when(userService).changePassword(eq(1L), any(PasswordChangeRequestDTO.class));

    // Act & Assert
    mockMvc.perform(patch("/api/users/1/password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordDTO)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("changePassword - Other User Password As Admin - Returns 204")
  @WithMockUser(username = "admin", roles = "ADMIN")
  void changePassword_OtherUserPasswordAsAdmin_Returns204() throws Exception {
    // Arrange
    PasswordChangeRequestDTO passwordDTO = new PasswordChangeRequestDTO();
    passwordDTO.setNewPassword("newpassword123");

    doNothing().when(userService).changePassword(eq(1L), any(PasswordChangeRequestDTO.class));

    // Act & Assert
    mockMvc.perform(patch("/api/users/1/password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordDTO)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("deleteUser - As Admin - Returns 204")
  @WithMockUser(roles = "ADMIN")
  void deleteUser_AsAdmin_Returns204() throws Exception {
    // Arrange
    doNothing().when(userService).deleteUser(1L);

    // Act & Assert
    mockMvc.perform(delete("/api/users/1"))
        .andExpect(status().isNoContent());
  }

  // Security test moved to UserControllerSecurityTest

  @Test
  @DisplayName("deleteUser - Non-Existing User - Returns 404")
  @WithMockUser(roles = "ADMIN")
  void deleteUser_NonExistingUser_Returns404() throws Exception {
    // Arrange
    doThrow(new ResourceNotFoundException("User not found with id: 999"))
        .when(userService).deleteUser(999L);

    // Act & Assert
    mockMvc.perform(delete("/api/users/999"))
        .andExpect(status().isNotFound());
  }
}
