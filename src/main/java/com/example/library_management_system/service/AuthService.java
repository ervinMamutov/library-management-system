package com.example.library_management_system.service;

import com.example.library_management_system.dto.auth.AuthResponseDTO;
import com.example.library_management_system.dto.auth.LoginRequestDTO;
import com.example.library_management_system.dto.auth.RegisterRequestDTO;
import com.example.library_management_system.exception.DuplicateResourceException;
import com.example.library_management_system.exception.UnauthorizedException;
import com.example.library_management_system.model.Role;
import com.example.library_management_system.model.User;
import com.example.library_management_system.repository.UserRepository;
import com.example.library_management_system.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public AuthService(UserRepository userRepository,
                     BCryptPasswordEncoder passwordEncoder,
                     JwtUtil jwtUtil) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
  }

  public AuthResponseDTO register(RegisterRequestDTO request) {
    if (userRepository.findByUsername(request.getUsername()).isPresent()) {
      throw new DuplicateResourceException("Username already exists: " + request.getUsername());
    }

    String hashedPassword = passwordEncoder.encode(request.getPassword());

    User user = new User(request.getUsername(), hashedPassword, Role.MEMBER);
    userRepository.save(user);

    String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

    return new AuthResponseDTO(token, user.getUsername(), user.getRole().name());
  }

  public AuthResponseDTO login(LoginRequestDTO request) {
    User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

    if (!passwordEncoder.matches(request.getPassword(), user.getHashedPassword())) {
      throw new UnauthorizedException("Invalid username or password");
    }

    String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

    return new AuthResponseDTO(token, user.getUsername(), user.getRole().name());
  }
}
