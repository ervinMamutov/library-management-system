package com.example.library_management_system.dto.book;

import jakarta.validation.constraints.*;

public class BookImportDTO {

  @NotBlank(message = "Title cannot be blank")
  @Size(min = 1, max = 200,
          message = "Title must be between 1 and 200 characters")
  private String title;

  @NotBlank(message = "Author cannot be blank")
  @Size(min = 1, max = 200,
          message = "Author must be between 1 and 200 characters")
  private String author;

  @NotBlank(message = "ISBN cannot be blank")
  private String isbn;

  @NotBlank(message = "Genre cannot be blank")
  @Size(min = 3, max = 100,
          message = "Genre must be between 3 and 100 characters")
  private String genre;

  @NotNull(message = "Copies cannot be null")
  @Min(value = 0, message = "Copies cannot be negative")
  private Integer copies;

  public BookImportDTO() {
  }

  public BookImportDTO(String title, String author, String isbn, String genre, Integer copies) {
    this.title = title;
    this.author = author;
    this.isbn = isbn;
    this.genre = genre;
    this.copies = copies;
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

  public Integer getCopies() {
    return copies;
  }

  public void setCopies(Integer copies) {
    this.copies = copies;
  }
}
