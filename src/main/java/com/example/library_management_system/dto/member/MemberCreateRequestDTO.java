package com.example.library_management_system.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MemberCreateRequestDTO {

  @NotBlank(message = "Name cannot be blank")
  @Size(min = 3, max = 100,
          message = "Name must be between 3 and 100 characters")
  private String name;

  @NotBlank(message = "Email cannot be blank")
  @Email(message = "Email must be valid")
  private String email;

  @NotBlank(message = "Phone cannot be blank")
  @Size(min = 10, max = 20,
          message = "Phone must be between 10 and 20 characters")
  private String phone;

  public MemberCreateRequestDTO() {
  }

  public MemberCreateRequestDTO(String name, String email, String phone) {
    this.name = name;
    this.email = email;
    this.phone = phone;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }
}
