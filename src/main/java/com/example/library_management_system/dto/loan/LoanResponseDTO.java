package com.example.library_management_system.dto.loan;

import java.time.LocalDateTime;

public class LoanResponseDTO {

  private Long id;
  private Long memberId;
  private Long bookId;
  private String memberName;
  private String bookTitle;
  private LocalDateTime borrowDate;
  private LocalDateTime dueDate;
  private LocalDateTime returnDate;
  private boolean isOverdue;

  public LoanResponseDTO() {
  }

  public LoanResponseDTO(Long id, Long memberId, Long bookId, String memberName, String bookTitle,
                         LocalDateTime borrowDate, LocalDateTime dueDate, LocalDateTime returnDate,
                         boolean isOverdue) {
    this.id = id;
    this.memberId = memberId;
    this.bookId = bookId;
    this.memberName = memberName;
    this.bookTitle = bookTitle;
    this.borrowDate = borrowDate;
    this.dueDate = dueDate;
    this.returnDate = returnDate;
    this.isOverdue = isOverdue;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public String getMemberName() {
    return memberName;
  }

  public void setMemberName(String memberName) {
    this.memberName = memberName;
  }

  public String getBookTitle() {
    return bookTitle;
  }

  public void setBookTitle(String bookTitle) {
    this.bookTitle = bookTitle;
  }

  public LocalDateTime getBorrowDate() {
    return borrowDate;
  }

  public void setBorrowDate(LocalDateTime borrowDate) {
    this.borrowDate = borrowDate;
  }

  public LocalDateTime getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDateTime dueDate) {
    this.dueDate = dueDate;
  }

  public LocalDateTime getReturnDate() {
    return returnDate;
  }

  public void setReturnDate(LocalDateTime returnDate) {
    this.returnDate = returnDate;
  }

  public boolean isOverdue() {
    return isOverdue;
  }

  public void setOverdue(boolean overdue) {
    isOverdue = overdue;
  }
}
