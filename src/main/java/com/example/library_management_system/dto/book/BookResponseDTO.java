package com.example.library_management_system.dto.book;

import java.time.LocalDateTime;

public class BookResponseDTO {

  private Long id;
  private String title;
  private String author;
  private String isbn;
  private String genre;
  private Integer publicationYear;
  private Integer copiesAvailable;
  private LocalDateTime createdAt;
  private String createdBy;
  private LocalDateTime updatedAt;
  private String updatedBy;

  public BookResponseDTO() {
  }

  public BookResponseDTO(Long id, String title, String author, String isbn, String genre,
                         Integer publicationYear, Integer copiesAvailable,
                         LocalDateTime createdAt, String createdBy,
                         LocalDateTime updatedAt, String updatedBy) {
    this.id = id;
    this.title = title;
    this.author = author;
    this.isbn = isbn;
    this.genre = genre;
    this.publicationYear = publicationYear;
    this.copiesAvailable = copiesAvailable;
    this.createdAt = createdAt;
    this.createdBy = createdBy;
    this.updatedAt = updatedAt;
    this.updatedBy = updatedBy;
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
}
