package com.example.library_management_system.dto.member;

import java.time.LocalDateTime;

public class MemberResponseDTO {

  private Long id;
  private String name;
  private String email;
  private String phone;
  private LocalDateTime membershipDate;

  public MemberResponseDTO() {
  }

  public MemberResponseDTO(Long id, String name, String email, String phone, LocalDateTime membershipDate) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.membershipDate = membershipDate;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public LocalDateTime getMembershipDate() {
    return membershipDate;
  }

  public void setMembershipDate(LocalDateTime membershipDate) {
    this.membershipDate = membershipDate;
  }
}
