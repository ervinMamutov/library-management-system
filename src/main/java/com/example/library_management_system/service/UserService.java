package com.example.library_management_system.service;

import com.example.library_management_system.dto.user.PasswordChangeRequestDTO;
import com.example.library_management_system.dto.user.UserResponseDTO;
import com.example.library_management_system.dto.user.UserUpdateRequestDTO;
import com.example.library_management_system.exception.DuplicateResourceException;
import com.example.library_management_system.exception.ResourceNotFoundException;
import com.example.library_management_system.model.Role;
import com.example.library_management_system.model.User;
import com.example.library_management_system.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public List<UserResponseDTO> findAllUsers() {
    return userRepository.findAll().stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
  }

  public UserResponseDTO findUserById(Long id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    return toResponseDTO(user);
  }

  public UserResponseDTO updateUser(Long id, UserUpdateRequestDTO request) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

    // Check if username is being changed and if it's already taken
    if (!user.getUsername().equals(request.getUsername())) {
      if (userRepository.findByUsername(request.getUsername()).isPresent()) {
        throw new DuplicateResourceException("Username already exists: " + request.getUsername());
      }
      user.setUsername(request.getUsername());
    }

    // Update role
    try {
      Role role = Role.valueOf(request.getRole().toUpperCase());
      user.setRole(role);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid role: " + request.getRole());
    }

    User updated = userRepository.save(user);
    return toResponseDTO(updated);
  }

  public UserResponseDTO updateRole(Long id, String roleName) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

    try {
      Role role = Role.valueOf(roleName.toUpperCase());
      user.setRole(role);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid role: " + roleName);
    }

    User updated = userRepository.save(user);
    return toResponseDTO(updated);
  }

  public void changePassword(Long id, PasswordChangeRequestDTO request) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

    String hashedPassword = passwordEncoder.encode(request.getNewPassword());
    user.setHashedPassword(hashedPassword);
    userRepository.save(user);
  }

  public void deleteUser(Long id) {
    if (!userRepository.existsById(id)) {
      throw new ResourceNotFoundException("User not found with id: " + id);
    }
    userRepository.deleteById(id);
  }

  private UserResponseDTO toResponseDTO(User user) {
    return new UserResponseDTO(
            user.getId(),
            user.getUsername(),
            user.getRole().name()
    );
  }
}
