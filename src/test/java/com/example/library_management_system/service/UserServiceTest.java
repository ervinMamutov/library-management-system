package com.example.library_management_system.service;

import com.example.library_management_system.dto.user.PasswordChangeRequestDTO;
import com.example.library_management_system.dto.user.UserResponseDTO;
import com.example.library_management_system.dto.user.UserUpdateRequestDTO;
import com.example.library_management_system.exception.DuplicateResourceException;
import com.example.library_management_system.exception.ResourceNotFoundException;
import com.example.library_management_system.model.Role;
import com.example.library_management_system.model.User;
import com.example.library_management_system.repository.UserRepository;
import com.example.library_management_system.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private BCryptPasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  private User testUser;
  private UserUpdateRequestDTO updateRequestDTO;

  @BeforeEach
  void setUp() {
    testUser = TestDataBuilder.createTestUserWithId(1L, Role.MEMBER);

    updateRequestDTO = new UserUpdateRequestDTO();
    updateRequestDTO.setUsername("updateduser");
    updateRequestDTO.setRole("LIBRARIAN");
  }

  @Test
  @DisplayName("findAllUsers - Returns All Users")
  void findAllUsers_ReturnsAllUsers() {
    // Arrange
    User user1 = TestDataBuilder.createTestUserWithId(1L, Role.MEMBER);
    User user2 = TestDataBuilder.createTestUserWithId(2L, Role.LIBRARIAN);
    User user3 = TestDataBuilder.createTestUserWithId(3L, Role.ADMIN);
    List<User> users = Arrays.asList(user1, user2, user3);

    when(userRepository.findAll()).thenReturn(users);

    // Act
    List<UserResponseDTO> result = userService.findAllUsers();

    // Assert
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getRole()).isEqualTo("MEMBER");
    assertThat(result.get(1).getRole()).isEqualTo("LIBRARIAN");
    assertThat(result.get(2).getRole()).isEqualTo("ADMIN");
    verify(userRepository).findAll();
  }

  @Test
  @DisplayName("findUserById - Existing ID - Returns User")
  void findUserById_ExistingId_ReturnsUser() {
    // Arrange
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    // Act
    UserResponseDTO result = userService.findUserById(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getUsername()).isEqualTo("testuser");
    assertThat(result.getRole()).isEqualTo("MEMBER");
    verify(userRepository).findById(1L);
  }

  @Test
  @DisplayName("findUserById - Non-Existing ID - Throws ResourceNotFoundException")
  void findUserById_NonExistingId_ThrowsResourceNotFoundException() {
    // Arrange
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> userService.findUserById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("User not found with id: 999");

    verify(userRepository).findById(999L);
  }

  @Test
  @DisplayName("updateUser - Valid Request - Returns Updated User")
  void updateUser_ValidRequest_ReturnsUpdatedUser() {
    // Arrange
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByUsername("updateduser")).thenReturn(Optional.empty());
    when(userRepository.save(testUser)).thenReturn(testUser);

    // Act
    UserResponseDTO result = userService.updateUser(1L, updateRequestDTO);

    // Assert
    assertThat(result).isNotNull();
    assertThat(testUser.getUsername()).isEqualTo("updateduser");
    assertThat(testUser.getRole()).isEqualTo(Role.LIBRARIAN);
    verify(userRepository).findById(1L);
    verify(userRepository).findByUsername("updateduser");
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("updateUser - Duplicate Username - Throws DuplicateResourceException")
  void updateUser_DuplicateUsername_ThrowsDuplicateResourceException() {
    // Arrange
    User existingUser = TestDataBuilder.createTestUserWithId(2L, Role.MEMBER);
    existingUser.setUsername("updateduser");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByUsername("updateduser")).thenReturn(Optional.of(existingUser));

    // Act & Assert
    assertThatThrownBy(() -> userService.updateUser(1L, updateRequestDTO))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("Username already exists: updateduser");

    verify(userRepository).findById(1L);
    verify(userRepository).findByUsername("updateduser");
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("updateUser - Invalid Role - Throws IllegalArgumentException")
  void updateUser_InvalidRole_ThrowsIllegalArgumentException() {
    // Arrange
    updateRequestDTO.setRole("INVALID_ROLE");
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> userService.updateUser(1L, updateRequestDTO))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid role: INVALID_ROLE");

    verify(userRepository).findById(1L);
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("updateRole - Valid Role - Updates Role")
  void updateRole_ValidRole_UpdatesRole() {
    // Arrange
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(testUser)).thenReturn(testUser);

    // Act
    UserResponseDTO result = userService.updateRole(1L, "ADMIN");

    // Assert
    assertThat(result).isNotNull();
    assertThat(testUser.getRole()).isEqualTo(Role.ADMIN);
    verify(userRepository).findById(1L);
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("updateRole - Invalid Role - Throws IllegalArgumentException")
  void updateRole_InvalidRole_ThrowsIllegalArgumentException() {
    // Arrange
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    // Act & Assert
    assertThatThrownBy(() -> userService.updateRole(1L, "INVALID_ROLE"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid role: INVALID_ROLE");

    verify(userRepository).findById(1L);
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("changePassword - Valid Request - Updates Password")
  void changePassword_ValidRequest_UpdatesPassword() {
    // Arrange
    PasswordChangeRequestDTO passwordDTO = new PasswordChangeRequestDTO();
    passwordDTO.setNewPassword("newpassword123");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.encode("newpassword123")).thenReturn("hashedNewPassword");
    when(userRepository.save(testUser)).thenReturn(testUser);

    // Act
    userService.changePassword(1L, passwordDTO);

    // Assert
    assertThat(testUser.getHashedPassword()).isEqualTo("hashedNewPassword");
    verify(userRepository).findById(1L);
    verify(passwordEncoder).encode("newpassword123");
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("deleteUser - Existing ID - Deletes User")
  void deleteUser_ExistingId_DeletesUser() {
    // Arrange
    when(userRepository.existsById(1L)).thenReturn(true);
    doNothing().when(userRepository).deleteById(1L);

    // Act
    userService.deleteUser(1L);

    // Assert
    verify(userRepository).existsById(1L);
    verify(userRepository).deleteById(1L);
  }

  @Test
  @DisplayName("deleteUser - Non-Existing ID - Throws ResourceNotFoundException")
  void deleteUser_NonExistingId_ThrowsResourceNotFoundException() {
    // Arrange
    when(userRepository.existsById(999L)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> userService.deleteUser(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("User not found with id: 999");

    verify(userRepository).existsById(999L);
    verify(userRepository, never()).deleteById(any());
  }
}
