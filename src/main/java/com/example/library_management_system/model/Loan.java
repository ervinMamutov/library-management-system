package com.example.library_management_system.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "loan")
public class Loan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;

  @Column(nullable = false)
  private LocalDateTime borrowDate;

  @Column(nullable = false)
  private LocalDateTime dueDate;

  @Column(nullable = true)
  private LocalDateTime returnDate;

  public Loan() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Member getMember() {
    return member;
  }

  public void setMember(Member member) {
    this.member = member;
  }

  public Book getBook() {
    return book;
  }

  public void setBook(Book book) {
    this.book = book;
  }

  public LocalDateTime getBorrowDate() {
    return borrowDate;
  }

  public void setBorrowDate(LocalDateTime borrowDate) {
    this.borrowDate = borrowDate;
  }

  public LocalDateTime getReturnDate() {
    return returnDate;
  }

  public void setReturnDate(LocalDateTime returnDate) {
    this.returnDate = returnDate;
  }

  public LocalDateTime getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDateTime dueDate) {
    this.dueDate = dueDate;
  }

  @Transient
  public boolean isOverdue() {
    return returnDate == null && dueDate != null &&
            LocalDateTime.now().isAfter(dueDate);
  }

  @PrePersist
  public void onCreate() {
    this.borrowDate = LocalDateTime.now();
  }

  @Override
  public String toString() {
    return "{Loan id=" + id + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Loan loan)) return false;
    return Objects.equals(id, loan.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}

