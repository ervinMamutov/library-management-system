package com.example.library_management_system.dto.loan;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class LoanCreateRequestDTO {

  @NotNull(message = "Member ID cannot be null")
  private Long memberId;

  @NotNull(message = "Book ID cannot be null")
  private Long bookId;

  private LocalDateTime dueDate; // Optional: defaults to 14 days from borrow date

  public LoanCreateRequestDTO() {
  }

  public LoanCreateRequestDTO(Long memberId, Long bookId, LocalDateTime dueDate) {
    this.memberId = memberId;
    this.bookId = bookId;
    this.dueDate = dueDate;
  }

  public Long getMemberId() {
    return memberId;
  }

  public void setMemberId(Long memberId) {
    this.memberId = memberId;
  }

  public Long getBookId() {
    return bookId;
  }

  public void setBookId(Long bookId) {
    this.bookId = bookId;
  }

  public LocalDateTime getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDateTime dueDate) {
    this.dueDate = dueDate;
  }
}
