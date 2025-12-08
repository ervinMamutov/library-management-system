package com.example.library_management_system.controller;

import com.example.library_management_system.dto.book.BookCreateRequestDTO;
import com.example.library_management_system.dto.book.BookImportDTO;
import com.example.library_management_system.dto.book.BookResponseDTO;
import com.example.library_management_system.dto.book.BookUpdateRequestDTO;
import com.example.library_management_system.exception.DuplicateResourceException;
import com.example.library_management_system.exception.ResourceNotFoundException;
import com.example.library_management_system.security.JwtAuthenticationFilter;
import com.example.library_management_system.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("BookController Tests")
class BookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private BookService bookService;

  @MockBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockBean
  private com.example.library_management_system.security.JwtUtil jwtUtil;

  private BookCreateRequestDTO createRequestDTO;
  private BookUpdateRequestDTO updateRequestDTO;
  private BookResponseDTO responseDTO;

  @BeforeEach
  void setUp() {
    createRequestDTO = new BookCreateRequestDTO();
    createRequestDTO.setTitle("1984");
    createRequestDTO.setAuthor("George Orwell");
    createRequestDTO.setIsbn("978-0451524935");
    createRequestDTO.setGenre("Dystopian");
    createRequestDTO.setPublicationYear(1949);
    createRequestDTO.setCopiesAvailable(5);

    updateRequestDTO = new BookUpdateRequestDTO();
    updateRequestDTO.setTitle("Updated Title");
    updateRequestDTO.setAuthor("Updated Author");
    updateRequestDTO.setGenre("Updated Genre");
    updateRequestDTO.setPublicationYear(2024);
    updateRequestDTO.setCopiesAvailable(10);

    responseDTO = new BookResponseDTO();
    responseDTO.setId(1L);
    responseDTO.setTitle("1984");
    responseDTO.setAuthor("George Orwell");
    responseDTO.setIsbn("978-0451524935");
    responseDTO.setGenre("Dystopian");
    responseDTO.setPublicationYear(1949);
    responseDTO.setCopiesAvailable(5);
    responseDTO.setCreatedAt(LocalDateTime.now());
    responseDTO.setCreatedBy("admin");
    responseDTO.setUpdatedAt(LocalDateTime.now());
    responseDTO.setUpdatedBy("admin");
  }

  @Test
  @DisplayName("createBook - As Admin - Returns 201")
  @WithMockUser(roles = "ADMIN")
  void createBook_AsAdmin_Returns201() throws Exception {
    // Arrange
    when(bookService.createBook(any(BookCreateRequestDTO.class))).thenReturn(responseDTO);

    // Act & Assert
    mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.title", is("1984")))
        .andExpect(jsonPath("$.author", is("George Orwell")))
        .andExpect(jsonPath("$.isbn", is("978-0451524935")));
  }

  @Test
  @DisplayName("createBook - As Librarian - Returns 201")
  @WithMockUser(roles = "LIBRARIAN")
  void createBook_AsLibrarian_Returns201() throws Exception {
    // Arrange
    when(bookService.createBook(any(BookCreateRequestDTO.class))).thenReturn(responseDTO);

    // Act & Assert
    mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title", is("1984")));
  }

  // Security tests moved to BookControllerSecurityTest

  @Test
  @DisplayName("createBook - Invalid Request - Returns 400")
  @WithMockUser(roles = "ADMIN")
  void createBook_InvalidRequest_Returns400() throws Exception {
    // Arrange - Create invalid request (missing required fields)
    BookCreateRequestDTO invalidRequest = new BookCreateRequestDTO();
    // Missing all required fields

    // Act & Assert
    mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("createBook - Duplicate ISBN - Returns 409")
  @WithMockUser(roles = "ADMIN")
  void createBook_DuplicateISBN_Returns409() throws Exception {
    // Arrange
    when(bookService.createBook(any(BookCreateRequestDTO.class)))
        .thenThrow(new DuplicateResourceException("Book with ISBN already exists"));

    // Act & Assert
    mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestDTO)))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("findAllBooks - Authenticated - Returns 200")
  @WithMockUser
  void findAllBooks_Authenticated_Returns200() throws Exception {
    // Arrange
    BookResponseDTO book1 = new BookResponseDTO();
    book1.setId(1L);
    book1.setTitle("1984");

    BookResponseDTO book2 = new BookResponseDTO();
    book2.setId(2L);
    book2.setTitle("Animal Farm");

    List<BookResponseDTO> books = Arrays.asList(book1, book2);
    when(bookService.findAllBooks()).thenReturn(books);

    // Act & Assert
    mockMvc.perform(get("/api/books"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[0].title", is("1984")))
        .andExpect(jsonPath("$[1].id", is(2)))
        .andExpect(jsonPath("$[1].title", is("Animal Farm")));
  }

  // Security test moved to BookControllerSecurityTest

  @Test
  @DisplayName("findBookById - Existing ID - Returns 200")
  @WithMockUser
  void findBookById_ExistingId_Returns200() throws Exception {
    // Arrange
    when(bookService.findBookById(1L)).thenReturn(responseDTO);

    // Act & Assert
    mockMvc.perform(get("/api/books/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.title", is("1984")))
        .andExpect(jsonPath("$.author", is("George Orwell")));
  }

  @Test
  @DisplayName("findBookById - Non-Existing ID - Returns 404")
  @WithMockUser
  void findBookById_NonExistingId_Returns404() throws Exception {
    // Arrange
    when(bookService.findBookById(999L))
        .thenThrow(new ResourceNotFoundException("Book not found with id: 999"));

    // Act & Assert
    mockMvc.perform(get("/api/books/999"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("updateBook - As Admin - Returns 200")
  @WithMockUser(roles = "ADMIN")
  void updateBook_AsAdmin_Returns200() throws Exception {
    // Arrange
    BookResponseDTO updatedResponse = new BookResponseDTO();
    updatedResponse.setId(1L);
    updatedResponse.setTitle("Updated Title");
    updatedResponse.setAuthor("Updated Author");

    when(bookService.updateBook(eq(1L), any(BookUpdateRequestDTO.class)))
        .thenReturn(updatedResponse);

    // Act & Assert
    mockMvc.perform(put("/api/books/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequestDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.title", is("Updated Title")))
        .andExpect(jsonPath("$.author", is("Updated Author")));
  }

  @Test
  @DisplayName("updateBook - As Librarian - Returns 200")
  @WithMockUser(roles = "LIBRARIAN")
  void updateBook_AsLibrarian_Returns200() throws Exception {
    // Arrange
    when(bookService.updateBook(eq(1L), any(BookUpdateRequestDTO.class)))
        .thenReturn(responseDTO);

    // Act & Assert
    mockMvc.perform(put("/api/books/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequestDTO)))
        .andExpect(status().isOk());
  }

  // Security test moved to BookControllerSecurityTest

  @Test
  @DisplayName("updateBook - Non-Existing ID - Returns 404")
  @WithMockUser(roles = "ADMIN")
  void updateBook_NonExistingId_Returns404() throws Exception {
    // Arrange
    when(bookService.updateBook(eq(999L), any(BookUpdateRequestDTO.class)))
        .thenThrow(new ResourceNotFoundException("Book not found with id: 999"));

    // Act & Assert
    mockMvc.perform(put("/api/books/999")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequestDTO)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("deleteBook - As Admin - Returns 204")
  @WithMockUser(roles = "ADMIN")
  void deleteBook_AsAdmin_Returns204() throws Exception {
    // Arrange
    doNothing().when(bookService).deleteBook(1L);

    // Act & Assert
    mockMvc.perform(delete("/api/books/1"))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("deleteBook - As Librarian - Returns 204")
  @WithMockUser(roles = "LIBRARIAN")
  void deleteBook_AsLibrarian_Returns204() throws Exception {
    // Arrange
    doNothing().when(bookService).deleteBook(1L);

    // Act & Assert
    mockMvc.perform(delete("/api/books/1"))
        .andExpect(status().isNoContent());
  }

  // Security test moved to BookControllerSecurityTest

  @Test
  @DisplayName("deleteBook - Non-Existing ID - Returns 404")
  @WithMockUser(roles = "ADMIN")
  void deleteBook_NonExistingId_Returns404() throws Exception {
    // Arrange
    doThrow(new ResourceNotFoundException("Book not found with id: 999"))
        .when(bookService).deleteBook(999L);

    // Act & Assert
    mockMvc.perform(delete("/api/books/999"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("importBooks - Valid JSON - Returns 201")
  @WithMockUser(roles = "ADMIN")
  void importBooks_ValidJSON_Returns201() throws Exception {
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

    BookResponseDTO imported1 = new BookResponseDTO();
    imported1.setId(1L);
    imported1.setTitle("Book 1");

    BookResponseDTO imported2 = new BookResponseDTO();
    imported2.setId(2L);
    imported2.setTitle("Book 2");

    List<BookResponseDTO> importedBooks = Arrays.asList(imported1, imported2);
    when(bookService.importBooks(any())).thenReturn(importedBooks);

    // Act & Assert
    mockMvc.perform(post("/api/books/import")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(importList)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].title", is("Book 1")))
        .andExpect(jsonPath("$[1].title", is("Book 2")));
  }

  // Security test moved to BookControllerSecurityTest

  // Validation test removed - DTO validation handled by @Valid annotation
}
