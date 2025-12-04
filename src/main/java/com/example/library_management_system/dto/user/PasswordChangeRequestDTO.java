package com.example.library_management_system.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordChangeRequestDTO {

  @NotBlank(message = "New password cannot be blank")
  @Size(min = 6, message = "Password must be at least 6 characters")
  private String newPassword;

  public PasswordChangeRequestDTO() {
  }

  public PasswordChangeRequestDTO(String newPassword) {
    this.newPassword = newPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }
}
