package com.example.library_management_system.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserUpdateRequestDTO {

  @NotBlank(message = "Username cannot be blank")
  @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
  private String username;

  @NotBlank(message = "Role cannot be blank")
  private String role;

  public UserUpdateRequestDTO() {
  }

  public UserUpdateRequestDTO(String username, String role) {
    this.username = username;
    this.role = role;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
