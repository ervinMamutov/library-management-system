package com.example.library_management_system.dto.book;

import jakarta.validation.constraints.*;

public class BookUpdateRequestDTO {

  @NotBlank(message = "Title cannot be blank")
  @Size(min = 1, max = 200,
          message = "Title must be between 1 and 200 characters")
  private String title;

  @NotBlank(message = "Author cannot be blank")
  @Size(min = 1, max = 200,
          message = "Author must be between 1 and 200 characters")
  private String author;

  @NotBlank(message = "Genre cannot be blank")
  @Size(min = 3, max = 100,
          message = "Genre must be between 3 and 100 characters")
  private String genre;

  @NotNull(message = "Publication year cannot be null")
  @Min(value = 1000, message = "Publication year must be at least 1000")
  @Max(value = 2100, message = "Publication year cannot be later than 2100")
  private Integer publicationYear;

  @NotNull(message = "Copies available cannot be null")
  @Min(value = 0, message = "Copies available cannot be negative")
  private Integer copiesAvailable;

  public BookUpdateRequestDTO() {
  }

  public BookUpdateRequestDTO(String title, String author, String genre,
                               Integer publicationYear, Integer copiesAvailable) {
    this.title = title;
    this.author = author;
    this.genre = genre;
    this.publicationYear = publicationYear;
    this.copiesAvailable = copiesAvailable;
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
}
