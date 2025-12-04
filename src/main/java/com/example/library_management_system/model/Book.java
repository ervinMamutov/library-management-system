package com.example.library_management_system.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "book")
@EntityListeners(AuditingEntityListener.class)
public class Book {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Title cannot be blank")
  @Size(min = 1, max = 200,
          message = "Title must be between 1 and 200 characters")
  private String title;

  @NotBlank(message = "Author cannot be blank")
  @Size(min = 1, max = 200,
          message = "Author must be between 1 and 200 characters")
  private String author;

  @NotBlank(message = "ISBN cannot be blank")
  @Column(unique = true, nullable = false)
  private String isbn;

  @NotBlank(message = "Genre cannot be blank")
  @Size(min = 3, max = 100,
          message = "Genre must be between 3 and 100 characters")
  private String genre;

  @NotNull(message = "PublicationYear cannot be null")
  @Min(value = 1000, message = "Publication year must be at least 1000")
  @Max(value = 2100, message = "Publication year cannot be later than 2100")
  private Integer publicationYear;

  @NotNull(message = "CopiesAvailable cannot be null")
  @Column(nullable = false)
  private Integer copiesAvailable;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @CreatedBy
  @Column(updatable = false, length = 100)
  private String createdBy;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  @LastModifiedBy
  @Column(length = 100)
  private String updatedBy;

  @ManyToOne
  @JoinColumn(name = "user_id")
  @JsonBackReference
  private User user;

  public Book() {
  }

  public Book(String title, String author, String isbn, String genre,
              Integer publicationYear, Integer copiesAvailable) {
    this.title = title;
    this.author = author;
    this.isbn = isbn;
    this.genre = genre;
    this.publicationYear = publicationYear;
    this.copiesAvailable = copiesAvailable;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getGenre() {
    return genre;
  }

  public void setGenre(String genre) {
    this.genre = genre;
  }

  public Integer getPublicationYear() {
    return publicationYear;
  }

  public void setPublicationYear(Integer publicationYear) {
    this.publicationYear = publicationYear;
  }

  public Integer getCopiesAvailable() {
    return copiesAvailable;
  }

  public void setCopiesAvailable(Integer copiesAvailable) {
    this.copiesAvailable = copiesAvailable;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  @Override
  public String toString() {
    return "{Book id=" + id + ", title='" + title + "', author='" +
            author + "', isbn='" + isbn + "', genre='" + genre +
            "', copiesAvailable=" + copiesAvailable +
            ", publicationYear=" + publicationYear + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Book book)) return false;
    return Objects.equals(id, book.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}

