package com.example.library_management_system.dto.book;

public class BookResponseDTO {

  private Long id;
  private String title;
  private String author;
  private String isbn;
  private String genre;
  private Integer publicationYear;
  private Integer copiesAvailable;

  public BookResponseDTO() {
  }

  public BookResponseDTO(Long id, String title, String author, String isbn, String genre,
                         Integer publicationYear, Integer copiesAvailable) {
    this.id = id;
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
}
