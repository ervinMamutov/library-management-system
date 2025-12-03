package com.example.library_management_system.mapper;

import com.example.library_management_system.dto.book.BookCreateRequestDTO;
import com.example.library_management_system.dto.book.BookResponseDTO;
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

  public BookResponseDTO toResponseDTO(Book book) {
    return new BookResponseDTO(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getIsbn(),
            book.getGenre(),
            book.getPublicationYear(),
            book.getCopiesAvailable()
    );
  }
}
