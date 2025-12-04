package com.example.library_management_system.service;

import com.example.library_management_system.dto.book.BookCreateRequestDTO;
import com.example.library_management_system.dto.book.BookImportDTO;
import com.example.library_management_system.dto.book.BookResponseDTO;
import com.example.library_management_system.dto.book.BookUpdateRequestDTO;
import com.example.library_management_system.exception.DuplicateResourceException;
import com.example.library_management_system.exception.ResourceNotFoundException;
import com.example.library_management_system.mapper.BookMapper;
import com.example.library_management_system.model.Book;
import com.example.library_management_system.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

  private final BookRepository bookRepository;
  private final BookMapper bookMapper;

  public BookService(BookRepository bookRepository, BookMapper bookMapper) {
    this.bookRepository = bookRepository;
    this.bookMapper = bookMapper;
  }

  public BookResponseDTO createBook(BookCreateRequestDTO request) {
    if (bookRepository.existsByIsbn(request.getIsbn())) {
      throw new DuplicateResourceException("Book with ISBN already exists: " + request.getIsbn());
    }

    Book book = bookMapper.toEntity(request);
    Book savedBook = bookRepository.save(book);

    return bookMapper.toResponseDTO(savedBook);
  }

  public List<BookResponseDTO> findAllBooks() {
    return bookRepository.findAll()
            .stream()
            .map(bookMapper::toResponseDTO)
            .collect(Collectors.toList());
  }

  public BookResponseDTO findBookById(Long id) {
    return bookRepository.findById(id)
            .map(bookMapper::toResponseDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
  }

  @Transactional
  public BookResponseDTO updateBook(Long id, BookUpdateRequestDTO request) {
    Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

    bookMapper.updateEntity(book, request);
    Book updatedBook = bookRepository.save(book);

    return bookMapper.toResponseDTO(updatedBook);
  }

  public void deleteBook(Long id) {
    Book book = bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

    bookRepository.delete(book);
  }

  @Transactional
  public List<BookResponseDTO> importBooks(List<BookImportDTO> books) {
    List<BookResponseDTO> importedBooks = new ArrayList<>();

    for (BookImportDTO importDTO : books) {
      if (bookRepository.existsByIsbn(importDTO.getIsbn())) {
        continue;
      }

      Book book = bookMapper.toEntity(importDTO);
      Book savedBook = bookRepository.save(book);
      importedBooks.add(bookMapper.toResponseDTO(savedBook));
    }

    return importedBooks;
  }

  @Transactional
  void decrementCopies(Long bookId) {
    Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

    book.setCopiesAvailable(book.getCopiesAvailable() - 1);
    bookRepository.save(book);
  }

  @Transactional
  void incrementCopies(Long bookId) {
    Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

    book.setCopiesAvailable(book.getCopiesAvailable() + 1);
    bookRepository.save(book);
  }
}
