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
import com.example.library_management_system.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Tests")
class BookServiceTest {

  @Mock
  private BookRepository bookRepository;

  @Mock
  private BookMapper bookMapper;

  @InjectMocks
  private BookService bookService;

  private Book testBook;
  private BookCreateRequestDTO createRequestDTO;
  private BookResponseDTO responseDTO;

  @BeforeEach
  void setUp() {
    testBook = TestDataBuilder.createTestBook();
    testBook.setId(1L);

    createRequestDTO = TestDataBuilder.createBookCreateRequestDTO();
    responseDTO = TestDataBuilder.createBookResponseDTO();
  }

  @Test
  @DisplayName("createBook - Valid Request - Returns Created Book")
  void createBook_ValidRequest_ReturnsCreatedBook() {
    // Arrange
    when(bookRepository.existsByIsbn(createRequestDTO.getIsbn())).thenReturn(false);
    when(bookMapper.toEntity(createRequestDTO)).thenReturn(testBook);
    when(bookRepository.save(any(Book.class))).thenReturn(testBook);
    when(bookMapper.toResponseDTO(testBook)).thenReturn(responseDTO);

    // Act
    BookResponseDTO result = bookService.createBook(createRequestDTO);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("1984");
    verify(bookRepository).existsByIsbn(createRequestDTO.getIsbn());
    verify(bookRepository).save(any(Book.class));
    verify(bookMapper).toEntity(createRequestDTO);
    verify(bookMapper).toResponseDTO(testBook);
  }

  @Test
  @DisplayName("createBook - Duplicate ISBN - Throws DuplicateResourceException")
  void createBook_DuplicateISBN_ThrowsDuplicateResourceException() {
    // Arrange
    when(bookRepository.existsByIsbn(createRequestDTO.getIsbn())).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> bookService.createBook(createRequestDTO))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("Book with ISBN already exists");

    verify(bookRepository).existsByIsbn(createRequestDTO.getIsbn());
    verify(bookRepository, never()).save(any(Book.class));
  }

  @Test
  @DisplayName("findAllBooks - Returns All Books")
  void findAllBooks_ReturnsAllBooks() {
    // Arrange
    Book book1 = TestDataBuilder.createTestBookWithId(1L);
    Book book2 = TestDataBuilder.createTestBookWithId(2L);
    List<Book> books = Arrays.asList(book1, book2);

    BookResponseDTO dto1 = TestDataBuilder.createBookResponseDTO();
    BookResponseDTO dto2 = TestDataBuilder.createBookResponseDTO();

    when(bookRepository.findAll()).thenReturn(books);
    when(bookMapper.toResponseDTO(book1)).thenReturn(dto1);
    when(bookMapper.toResponseDTO(book2)).thenReturn(dto2);

    // Act
    List<BookResponseDTO> result = bookService.findAllBooks();

    // Assert
    assertThat(result).hasSize(2);
    verify(bookRepository).findAll();
    verify(bookMapper, times(2)).toResponseDTO(any(Book.class));
  }

  @Test
  @DisplayName("findBookById - Existing ID - Returns Book")
  void findBookById_ExistingId_ReturnsBook() {
    // Arrange
    when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
    when(bookMapper.toResponseDTO(testBook)).thenReturn(responseDTO);

    // Act
    BookResponseDTO result = bookService.findBookById(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    verify(bookRepository).findById(1L);
    verify(bookMapper).toResponseDTO(testBook);
  }

  @Test
  @DisplayName("findBookById - Non-Existing ID - Throws ResourceNotFoundException")
  void findBookById_NonExistingId_ThrowsResourceNotFoundException() {
    // Arrange
    when(bookRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> bookService.findBookById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Book not found with id: 999");

    verify(bookRepository).findById(999L);
    verify(bookMapper, never()).toResponseDTO(any());
  }

  @Test
  @DisplayName("updateBook - Valid Request - Returns Updated Book")
  void updateBook_ValidRequest_ReturnsUpdatedBook() {
    // Arrange
    BookUpdateRequestDTO updateDTO = new BookUpdateRequestDTO();
    updateDTO.setTitle("Updated Title");
    updateDTO.setAuthor("Updated Author");
    updateDTO.setGenre("Updated Genre");
    updateDTO.setPublicationYear(2024);
    updateDTO.setCopiesAvailable(10);

    when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
    doNothing().when(bookMapper).updateEntity(testBook, updateDTO);
    when(bookRepository.save(testBook)).thenReturn(testBook);
    when(bookMapper.toResponseDTO(testBook)).thenReturn(responseDTO);

    // Act
    BookResponseDTO result = bookService.updateBook(1L, updateDTO);

    // Assert
    assertThat(result).isNotNull();
    verify(bookRepository).findById(1L);
    verify(bookMapper).updateEntity(testBook, updateDTO);
    verify(bookRepository).save(testBook);
    verify(bookMapper).toResponseDTO(testBook);
  }

  @Test
  @DisplayName("updateBook - Non-Existing ID - Throws ResourceNotFoundException")
  void updateBook_NonExistingId_ThrowsResourceNotFoundException() {
    // Arrange
    BookUpdateRequestDTO updateDTO = new BookUpdateRequestDTO();
    when(bookRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> bookService.updateBook(999L, updateDTO))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Book not found with id: 999");

    verify(bookRepository).findById(999L);
    verify(bookRepository, never()).save(any());
  }

  @Test
  @DisplayName("deleteBook - Existing ID - Deletes Book")
  void deleteBook_ExistingId_DeletesBook() {
    // Arrange
    when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
    doNothing().when(bookRepository).delete(testBook);

    // Act
    bookService.deleteBook(1L);

    // Assert
    verify(bookRepository).findById(1L);
    verify(bookRepository).delete(testBook);
  }

  @Test
  @DisplayName("deleteBook - Non-Existing ID - Throws ResourceNotFoundException")
  void deleteBook_NonExistingId_ThrowsResourceNotFoundException() {
    // Arrange
    when(bookRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> bookService.deleteBook(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Book not found with id: 999");

    verify(bookRepository).findById(999L);
    verify(bookRepository, never()).delete(any());
  }

  @Test
  @DisplayName("importBooks - Valid List - Returns Imported Books")
  void importBooks_ValidList_ReturnsImportedBooks() {
    // Arrange
    BookImportDTO importDTO1 = new BookImportDTO();
    importDTO1.setIsbn("978-1111111111");
    importDTO1.setTitle("Book 1");
    importDTO1.setAuthor("Author 1");
    importDTO1.setGenre("Genre 1");
    importDTO1.setPublicationYear(2020);
    importDTO1.setCopies(3);

    BookImportDTO importDTO2 = new BookImportDTO();
    importDTO2.setIsbn("978-2222222222");
    importDTO2.setTitle("Book 2");
    importDTO2.setAuthor("Author 2");
    importDTO2.setGenre("Genre 2");
    importDTO2.setPublicationYear(2021);
    importDTO2.setCopies(5);

    List<BookImportDTO> importList = Arrays.asList(importDTO1, importDTO2);

    Book book1 = TestDataBuilder.createTestBookWithISBN("978-1111111111");
    Book book2 = TestDataBuilder.createTestBookWithISBN("978-2222222222");

    when(bookRepository.existsByIsbn(importDTO1.getIsbn())).thenReturn(false);
    when(bookRepository.existsByIsbn(importDTO2.getIsbn())).thenReturn(false);
    when(bookMapper.toEntity(importDTO1)).thenReturn(book1);
    when(bookMapper.toEntity(importDTO2)).thenReturn(book2);
    when(bookRepository.save(book1)).thenReturn(book1);
    when(bookRepository.save(book2)).thenReturn(book2);
    when(bookMapper.toResponseDTO(any(Book.class))).thenReturn(responseDTO);

    // Act
    List<BookResponseDTO> result = bookService.importBooks(importList);

    // Assert
    assertThat(result).hasSize(2);
    verify(bookRepository, times(2)).existsByIsbn(anyString());
    verify(bookRepository, times(2)).save(any(Book.class));
    verify(bookMapper, times(2)).toEntity(any(BookImportDTO.class));
  }

  @Test
  @DisplayName("importBooks - Duplicate ISBN - Skips Duplicate")
  void importBooks_DuplicateISBN_SkipsDuplicate() {
    // Arrange
    BookImportDTO importDTO1 = new BookImportDTO();
    importDTO1.setIsbn("978-1111111111");
    importDTO1.setTitle("Book 1");

    BookImportDTO importDTO2 = new BookImportDTO();
    importDTO2.setIsbn("978-2222222222");
    importDTO2.setTitle("Book 2");

    List<BookImportDTO> importList = Arrays.asList(importDTO1, importDTO2);

    Book book2 = TestDataBuilder.createTestBookWithISBN("978-2222222222");

    when(bookRepository.existsByIsbn("978-1111111111")).thenReturn(true); // Duplicate
    when(bookRepository.existsByIsbn("978-2222222222")).thenReturn(false);
    when(bookMapper.toEntity(importDTO2)).thenReturn(book2);
    when(bookRepository.save(book2)).thenReturn(book2);
    when(bookMapper.toResponseDTO(book2)).thenReturn(responseDTO);

    // Act
    List<BookResponseDTO> result = bookService.importBooks(importList);

    // Assert
    assertThat(result).hasSize(1); // Only one book imported (duplicate skipped)
    verify(bookRepository, times(2)).existsByIsbn(anyString());
    verify(bookRepository, times(1)).save(any(Book.class)); // Only saved once
  }

  @Test
  @DisplayName("decrementCopies - Available Copies - Decrements By One")
  void decrementCopies_AvailableCopies_DecrementsByOne() {
    // Arrange
    testBook.setCopiesAvailable(5);
    when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
    when(bookRepository.save(testBook)).thenReturn(testBook);

    // Act
    bookService.decrementCopies(1L);

    // Assert
    assertThat(testBook.getCopiesAvailable()).isEqualTo(4);
    verify(bookRepository).findById(1L);
    verify(bookRepository).save(testBook);
  }

  @Test
  @DisplayName("incrementCopies - Increments By One")
  void incrementCopies_IncrementsByOne() {
    // Arrange
    testBook.setCopiesAvailable(5);
    when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
    when(bookRepository.save(testBook)).thenReturn(testBook);

    // Act
    bookService.incrementCopies(1L);

    // Assert
    assertThat(testBook.getCopiesAvailable()).isEqualTo(6);
    verify(bookRepository).findById(1L);
    verify(bookRepository).save(testBook);
  }
}
