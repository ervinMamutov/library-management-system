package com.example.library_management_system.controller;

import com.example.library_management_system.dto.book.BookCreateRequestDTO;
import com.example.library_management_system.dto.book.BookImportDTO;
import com.example.library_management_system.dto.book.BookResponseDTO;
import com.example.library_management_system.dto.book.BookUpdateRequestDTO;
import com.example.library_management_system.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
  public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody BookCreateRequestDTO request) {
    BookResponseDTO created = bookService.createBook(request);
    return new ResponseEntity<>(created, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<List<BookResponseDTO>> findAllBooks() {
    List<BookResponseDTO> books = bookService.findAllBooks();
    return ResponseEntity.ok(books);
  }

  @GetMapping("/{id}")
  public ResponseEntity<BookResponseDTO> findBookById(@PathVariable Long id) {
    BookResponseDTO book = bookService.findBookById(id);
    return ResponseEntity.ok(book);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
  public ResponseEntity<BookResponseDTO> updateBook(@PathVariable Long id,
                                                      @Valid @RequestBody BookUpdateRequestDTO request) {
    BookResponseDTO updated = bookService.updateBook(id, request);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
  public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
    bookService.deleteBook(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/import")
  @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
  public ResponseEntity<List<BookResponseDTO>> importBooks(@Valid @RequestBody List<BookImportDTO> books) {
    List<BookResponseDTO> imported = bookService.importBooks(books);
    return new ResponseEntity<>(imported, HttpStatus.CREATED);
  }
}
