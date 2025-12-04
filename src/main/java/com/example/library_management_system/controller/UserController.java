package com.example.library_management_system.controller;

import com.example.library_management_system.dto.user.PasswordChangeRequestDTO;
import com.example.library_management_system.dto.user.UserResponseDTO;
import com.example.library_management_system.dto.user.UserUpdateRequestDTO;
import com.example.library_management_system.exception.UnauthorizedException;
import com.example.library_management_system.model.User;
import com.example.library_management_system.repository.UserRepository;
import com.example.library_management_system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;
  private final UserRepository userRepository;

  public UserController(UserService userService, UserRepository userRepository) {
    this.userService = userService;
    this.userRepository = userRepository;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<UserResponseDTO>> findAllUsers() {
    List<UserResponseDTO> users = userService.findAllUsers();
    return ResponseEntity.ok(users);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponseDTO> findUserById(@PathVariable Long id) {
    UserResponseDTO user = userService.findUserById(id);
    return ResponseEntity.ok(user);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id,
                                                      @Valid @RequestBody UserUpdateRequestDTO request) {
    UserResponseDTO updated = userService.updateUser(id, request);
    return ResponseEntity.ok(updated);
  }

  @PatchMapping("/{id}/role")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponseDTO> updateRole(@PathVariable Long id,
                                                      @RequestBody Map<String, String> request) {
    String role = request.get("role");
    if (role == null || role.isBlank()) {
      throw new IllegalArgumentException("Role cannot be blank");
    }
    UserResponseDTO updated = userService.updateRole(id, role);
    return ResponseEntity.ok(updated);
  }

  @PatchMapping("/{id}/password")
  public ResponseEntity<Void> changePassword(@PathVariable Long id,
                                              @Valid @RequestBody PasswordChangeRequestDTO request) {
    // Check if current user is ADMIN or changing their own password
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String currentUsername = auth.getPrincipal().toString();

    boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    if (!isAdmin) {
      // Not admin - verify they're changing their own password
      User currentUser = userRepository.findByUsername(currentUsername)
              .orElseThrow(() -> new UnauthorizedException("User not found"));

      if (!currentUser.getId().equals(id)) {
        throw new UnauthorizedException("You can only change your own password");
      }
    }

    userService.changePassword(id, request);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
