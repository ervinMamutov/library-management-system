package com.example.library_management_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "member")
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Name cannot be blank")
  @Size(min = 3, max = 100,
          message = "Name must be between 3 and 100 characters")
  private String name;

  @NotBlank(message = "Email cannot be blank")
  @Email
  private String email;

  @NotBlank(message = "Phone cannot be blank")
  @Size(min = 10, max = 20,
          message = "Phone must be between 10 and 20 characters")
  private String phone;

  private LocalDateTime membershipDate;

  public Member() {
  }

  public Member(String name, String email, String phone) {
    this.name = name;
    this.email = email;
    this.phone = phone;
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

  @PrePersist
  protected void onCreate() {
    this.membershipDate = LocalDateTime.now();
  }

  @Override
  public String toString() {
    return "{Member id=" + id + ", name='" + name + "', email='" +
            email + "', phone='" + phone + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Member member)) return false;
    return Objects.equals(id, member.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
