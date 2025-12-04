package com.example.library_management_system.mapper;

import com.example.library_management_system.dto.book.BookCreateRequestDTO;
import com.example.library_management_system.dto.book.BookImportDTO;
import com.example.library_management_system.dto.book.BookResponseDTO;
import com.example.library_management_system.dto.book.BookUpdateRequestDTO;
import com.example.library_management_system.model.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

  public Book toEntity(BookCreateRequestDTO dto) {
    return new Book(
            dto.getTitle(),
            dto.getAuthor(),
            dto.getIsbn(),
            dto.getGenre(),
            dto.getPublicationYear(),
            dto.getCopiesAvailable()
    );
  }

  public Book toEntity(BookImportDTO dto) {
    return new Book(
            dto.getTitle(),
            dto.getAuthor(),
            dto.getIsbn(),
            dto.getGenre(),
            dto.getPublicationYear(),
            dto.getCopies()
    );
  }

  public void updateEntity(Book book, BookUpdateRequestDTO dto) {
    book.setTitle(dto.getTitle());
    book.setAuthor(dto.getAuthor());
    book.setGenre(dto.getGenre());
    book.setPublicationYear(dto.getPublicationYear());
    book.setCopiesAvailable(dto.getCopiesAvailable());
  }

  public BookResponseDTO toResponseDTO(Book book) {
    return new BookResponseDTO(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getIsbn(),
            book.getGenre(),
            book.getPublicationYear(),
            book.getCopiesAvailable(),
            book.getCreatedAt(),
            book.getCreatedBy(),
            book.getUpdatedAt(),
            book.getUpdatedBy()
    );
  }
}
