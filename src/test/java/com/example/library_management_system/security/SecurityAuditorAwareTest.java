package com.example.library_management_system.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("SecurityAuditorAware Tests")
class SecurityAuditorAwareTest {

  private SecurityAuditorAware auditorAware;

  @BeforeEach
  void setUp() {
    auditorAware = new SecurityAuditorAware();
    // Clear SecurityContext before each test
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    // Clean up SecurityContext after each test
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("getCurrentAuditor - Authenticated User - Returns Username")
  void getCurrentAuditor_AuthenticatedUser_ReturnsUsername() {
    // Arrange
    String username = "testuser";
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        username,
        "password",
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
    );

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    // Act
    Optional<String> result = auditorAware.getCurrentAuditor();

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo("testuser");
  }

  @Test
  @DisplayName("getCurrentAuditor - Not Authenticated - Returns Empty")
  void getCurrentAuditor_NotAuthenticated_ReturnsEmpty() {
    // Arrange
    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(false);
    when(authentication.getPrincipal()).thenReturn("testuser");

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    // Act
    Optional<String> result = auditorAware.getCurrentAuditor();

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("getCurrentAuditor - Anonymous User - Returns Empty")
  void getCurrentAuditor_AnonymousUser_ReturnsEmpty() {
    // Arrange
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        "anonymousUser",
        null,
        Collections.emptyList()
    );

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    // Act
    Optional<String> result = auditorAware.getCurrentAuditor();

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("getCurrentAuditor - Null Authentication - Returns Empty")
  void getCurrentAuditor_NullAuthentication_ReturnsEmpty() {
    // Arrange
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(null);
    SecurityContextHolder.setContext(securityContext);

    // Act
    Optional<String> result = auditorAware.getCurrentAuditor();

    // Assert
    assertThat(result).isEmpty();
  }
}
