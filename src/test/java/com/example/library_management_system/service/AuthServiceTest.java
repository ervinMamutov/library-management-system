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
import com.example.library_management_system.util.TestDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private BCryptPasswordEncoder passwordEncoder;

  @Mock
  private JwtUtil jwtUtil;

  @InjectMocks
  private AuthService authService;

  private RegisterRequestDTO registerRequestDTO;
  private LoginRequestDTO loginRequestDTO;
  private User testUser;

  @BeforeEach
  void setUp() {
    registerRequestDTO = new RegisterRequestDTO();
    registerRequestDTO.setUsername("testuser");
    registerRequestDTO.setPassword("password123");
    registerRequestDTO.setRole("MEMBER");

    loginRequestDTO = new LoginRequestDTO();
    loginRequestDTO.setUsername("testuser");
    loginRequestDTO.setPassword("password123");

    testUser = TestDataBuilder.createTestUser(Role.MEMBER);
    testUser.setId(1L);

    // Clear SecurityContext before each test
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    // Clean up SecurityContext after each test
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("register - Valid Request - Creates User With Member Role")
  void register_ValidRequest_CreatesUserWithMemberRole() {
    // Arrange
    when(userRepository.findByUsername(registerRequestDTO.getUsername())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(registerRequestDTO.getPassword())).thenReturn("hashedpassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(jwtUtil.generateToken("testuser", "MEMBER")).thenReturn("jwt-token");

    // Act
    AuthResponseDTO result = authService.register(registerRequestDTO);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("testuser");
    assertThat(result.getRole()).isEqualTo("MEMBER");
    assertThat(result.getToken()).isEqualTo("jwt-token");

    verify(userRepository).findByUsername("testuser");
    verify(passwordEncoder).encode("password123");
    verify(userRepository).save(any(User.class));
    verify(jwtUtil).generateToken("testuser", "MEMBER");
  }

  @Test
  @DisplayName("register - Admin Creates Librarian - Creates Librarian Role")
  void register_AdminCreatesLibrarian_CreatesLibrarianRole() {
    // Arrange
    registerRequestDTO.setRole("LIBRARIAN");

    // Set up ADMIN authentication in SecurityContext
    Authentication adminAuth = new UsernamePasswordAuthenticationToken(
        "admin",
        null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
    );
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(adminAuth);
    SecurityContextHolder.setContext(securityContext);

    User librarianUser = TestDataBuilder.createTestUser(Role.LIBRARIAN);
    librarianUser.setUsername("librarian");

    when(userRepository.findByUsername("librarian")).thenReturn(Optional.empty());
    when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");
    when(userRepository.save(any(User.class))).thenReturn(librarianUser);
    when(jwtUtil.generateToken("librarian", "LIBRARIAN")).thenReturn("jwt-token");

    registerRequestDTO.setUsername("librarian");

    // Act
    AuthResponseDTO result = authService.register(registerRequestDTO);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getRole()).isEqualTo("LIBRARIAN");
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("register - Admin Creates Admin - Creates Admin Role")
  void register_AdminCreatesAdmin_CreatesAdminRole() {
    // Arrange
    registerRequestDTO.setRole("ADMIN");
    registerRequestDTO.setUsername("newadmin");

    // Set up ADMIN authentication
    Authentication adminAuth = new UsernamePasswordAuthenticationToken(
        "admin",
        null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
    );
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(adminAuth);
    SecurityContextHolder.setContext(securityContext);

    User adminUser = TestDataBuilder.createTestUser(Role.ADMIN);
    adminUser.setUsername("newadmin");

    when(userRepository.findByUsername("newadmin")).thenReturn(Optional.empty());
    when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");
    when(userRepository.save(any(User.class))).thenReturn(adminUser);
    when(jwtUtil.generateToken("newadmin", "ADMIN")).thenReturn("jwt-token");

    // Act
    AuthResponseDTO result = authService.register(registerRequestDTO);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getRole()).isEqualTo("ADMIN");
  }

  @Test
  @DisplayName("register - Non-Admin Specifies Role - Ignores Role And Creates Member")
  void register_NonAdminSpecifiesRole_IgnoresRoleAndCreatesMember() {
    // Arrange
    registerRequestDTO.setRole("ADMIN"); // User tries to create ADMIN

    // Set up MEMBER authentication (not admin)
    Authentication memberAuth = new UsernamePasswordAuthenticationToken(
        "member",
        null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEMBER"))
    );
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(memberAuth);
    SecurityContextHolder.setContext(securityContext);

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
    when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(jwtUtil.generateToken("testuser", "MEMBER")).thenReturn("jwt-token");

    // Act
    AuthResponseDTO result = authService.register(registerRequestDTO);

    // Assert
    assertThat(result.getRole()).isEqualTo("MEMBER"); // Should be MEMBER, not ADMIN
  }

  @Test
  @DisplayName("register - Duplicate Username - Throws DuplicateResourceException")
  void register_DuplicateUsername_ThrowsDuplicateResourceException() {
    // Arrange
    when(userRepository.findByUsername(registerRequestDTO.getUsername()))
        .thenReturn(Optional.of(testUser));

    // Act & Assert
    assertThatThrownBy(() -> authService.register(registerRequestDTO))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("Username already exists");

    verify(userRepository).findByUsername("testuser");
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("register - Invalid Role - Throws IllegalArgumentException")
  void register_InvalidRole_ThrowsIllegalArgumentException() {
    // Arrange
    registerRequestDTO.setRole("INVALID_ROLE");

    // Set up ADMIN authentication
    Authentication adminAuth = new UsernamePasswordAuthenticationToken(
        "admin",
        null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
    );
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(adminAuth);
    SecurityContextHolder.setContext(securityContext);

    when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
    when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");

    // Act & Assert
    assertThatThrownBy(() -> authService.register(registerRequestDTO))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid role");

    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("login - Valid Credentials - Returns Token")
  void login_ValidCredentials_ReturnsToken() {
    // Arrange
    testUser.setHashedPassword("hashedpassword");
    when(userRepository.findByUsername(loginRequestDTO.getUsername()))
        .thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginRequestDTO.getPassword(), testUser.getHashedPassword()))
        .thenReturn(true);
    when(jwtUtil.generateToken("testuser", "MEMBER")).thenReturn("jwt-token");

    // Act
    AuthResponseDTO result = authService.login(loginRequestDTO);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("testuser");
    assertThat(result.getToken()).isEqualTo("jwt-token");
    assertThat(result.getRole()).isEqualTo("MEMBER");

    verify(userRepository).findByUsername("testuser");
    verify(passwordEncoder).matches("password123", "hashedpassword");
    verify(jwtUtil).generateToken("testuser", "MEMBER");
  }

  @Test
  @DisplayName("login - Invalid Username - Throws UnauthorizedException")
  void login_InvalidUsername_ThrowsUnauthorizedException() {
    // Arrange
    when(userRepository.findByUsername(loginRequestDTO.getUsername()))
        .thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> authService.login(loginRequestDTO))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessageContaining("Invalid username or password");

    verify(userRepository).findByUsername("testuser");
    verify(passwordEncoder, never()).matches(anyString(), anyString());
  }

  @Test
  @DisplayName("login - Invalid Password - Throws UnauthorizedException")
  void login_InvalidPassword_ThrowsUnauthorizedException() {
    // Arrange
    testUser.setHashedPassword("hashedpassword");
    when(userRepository.findByUsername(loginRequestDTO.getUsername()))
        .thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginRequestDTO.getPassword(), testUser.getHashedPassword()))
        .thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> authService.login(loginRequestDTO))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessageContaining("Invalid username or password");

    verify(userRepository).findByUsername("testuser");
    verify(passwordEncoder).matches("password123", "hashedpassword");
    verify(jwtUtil, never()).generateToken(anyString(), anyString());
  }
}
