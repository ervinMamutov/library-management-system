package com.example.library_management_system.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequestDTO {

  @NotBlank(message = "Username cannot be blank")
  @Size(min = 3, max = 100,
          message = "Username must be between 3 and 100 characters")
  private String username;

  @NotBlank(message = "Password cannot be blank")
  @Size(min = 6, message = "Password must be at least 6 characters")
  private String password;

  @NotBlank(message = "Role cannot be blank")
  private String role;

  public RegisterRequestDTO() {
  }

  public RegisterRequestDTO(String username, String password, String role) {
    this.username = username;
    this.password = password;
    this.role = role;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
