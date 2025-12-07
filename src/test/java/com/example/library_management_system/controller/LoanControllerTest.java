package com.example.library_management_system.controller;

import com.example.library_management_system.dto.loan.LoanCreateRequestDTO;
import com.example.library_management_system.dto.loan.LoanResponseDTO;
import com.example.library_management_system.dto.loan.LoanReturnRequestDTO;
import com.example.library_management_system.exception.BookUnavailableException;
import com.example.library_management_system.exception.InvalidLoanOperationException;
import com.example.library_management_system.exception.ResourceNotFoundException;
import com.example.library_management_system.security.JwtAuthenticationFilter;
import com.example.library_management_system.service.LoanService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("LoanController Tests")
class LoanControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private LoanService loanService;

  @MockBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockBean
  private com.example.library_management_system.security.JwtUtil jwtUtil;

  private LoanCreateRequestDTO createRequestDTO;
  private LoanResponseDTO responseDTO;

  @BeforeEach
  void setUp() {
    createRequestDTO = new LoanCreateRequestDTO();
    createRequestDTO.setMemberId(1L);
    createRequestDTO.setBookId(2L);
    createRequestDTO.setDueDate(LocalDateTime.now().plusDays(14));

    responseDTO = new LoanResponseDTO();
    responseDTO.setId(1L);
    responseDTO.setMemberId(1L);
    responseDTO.setBookId(2L);
    responseDTO.setBorrowDate(LocalDateTime.now());
    responseDTO.setDueDate(LocalDateTime.now().plusDays(14));
    responseDTO.setReturnDate(null);
  }

  @Test
  @DisplayName("borrowBook - As Admin - Returns 201")
  @WithMockUser(roles = "ADMIN")
  void borrowBook_AsAdmin_Returns201() throws Exception {
    // Arrange
    when(loanService.borrowBook(any(LoanCreateRequestDTO.class))).thenReturn(responseDTO);

    // Act & Assert
    mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.memberId", is(1)))
        .andExpect(jsonPath("$.bookId", is(2)));
  }

  @Test
  @DisplayName("borrowBook - As Librarian - Returns 201")
  @WithMockUser(roles = "LIBRARIAN")
  void borrowBook_AsLibrarian_Returns201() throws Exception {
    // Arrange
    when(loanService.borrowBook(any(LoanCreateRequestDTO.class))).thenReturn(responseDTO);

    // Act & Assert
    mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.memberId", is(1)))
        .andExpect(jsonPath("$.bookId", is(2)));
  }

  @Test
  @DisplayName("borrowBook - As Member - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void borrowBook_AsMember_Returns403() throws Exception {
    // Act & Assert
    mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestDTO)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("borrowBook - Without DueDate - Uses Default 14 Days")
  @WithMockUser(roles = "ADMIN")
  void borrowBook_WithoutDueDate_UsesDefault14Days() throws Exception {
    // Arrange
    createRequestDTO.setDueDate(null); // No due date provided
    when(loanService.borrowBook(any(LoanCreateRequestDTO.class))).thenReturn(responseDTO);

    // Act & Assert
    mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)));
  }

  @Test
  @DisplayName("borrowBook - Invalid Request - Returns 400")
  @WithMockUser(roles = "ADMIN")
  void borrowBook_InvalidRequest_Returns400() throws Exception {
    // Arrange - Create invalid request (missing required fields)
    LoanCreateRequestDTO invalidRequest = new LoanCreateRequestDTO();
    // Missing memberId and bookId

    // Act & Assert
    mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("borrowBook - Book Unavailable - Returns 400")
  @WithMockUser(roles = "ADMIN")
  void borrowBook_BookUnavailable_Returns400() throws Exception {
    // Arrange
    when(loanService.borrowBook(any(LoanCreateRequestDTO.class)))
        .thenThrow(new BookUnavailableException("Book is not available"));

    // Act & Assert
    mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("returnBook - As Librarian - Returns 200")
  @WithMockUser(roles = "LIBRARIAN")
  void returnBook_AsLibrarian_Returns200() throws Exception {
    // Arrange
    LoanReturnRequestDTO returnDTO = new LoanReturnRequestDTO();
    returnDTO.setReturnDate(LocalDateTime.now());

    LoanResponseDTO returnedLoan = new LoanResponseDTO();
    returnedLoan.setId(1L);
    returnedLoan.setMemberId(1L);
    returnedLoan.setBookId(2L);
    returnedLoan.setReturnDate(LocalDateTime.now());

    when(loanService.returnBook(eq(1L), any())).thenReturn(returnedLoan);

    // Act & Assert
    mockMvc.perform(patch("/api/loans/1/return")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(returnDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.returnDate").exists());
  }

  @Test
  @DisplayName("returnBook - As Admin - Returns 200")
  @WithMockUser(roles = "ADMIN")
  void returnBook_AsAdmin_Returns200() throws Exception {
    // Arrange
    LoanReturnRequestDTO returnDTO = new LoanReturnRequestDTO();
    returnDTO.setReturnDate(LocalDateTime.now());

    LoanResponseDTO returnedLoan = new LoanResponseDTO();
    returnedLoan.setId(1L);
    returnedLoan.setReturnDate(LocalDateTime.now());

    when(loanService.returnBook(eq(1L), any())).thenReturn(returnedLoan);

    // Act & Assert
    mockMvc.perform(patch("/api/loans/1/return")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(returnDTO)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("returnBook - As Member - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void returnBook_AsMember_Returns403() throws Exception {
    // Arrange
    LoanReturnRequestDTO returnDTO = new LoanReturnRequestDTO();

    // Act & Assert
    mockMvc.perform(patch("/api/loans/1/return")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(returnDTO)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("returnBook - Non-Existing Loan - Returns 404")
  @WithMockUser(roles = "ADMIN")
  void returnBook_NonExistingLoan_Returns404() throws Exception {
    // Arrange
    LoanReturnRequestDTO returnDTO = new LoanReturnRequestDTO();
    when(loanService.returnBook(eq(999L), any()))
        .thenThrow(new ResourceNotFoundException("Loan not found with id: 999"));

    // Act & Assert
    mockMvc.perform(patch("/api/loans/999/return")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(returnDTO)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("returnBook - Already Returned - Returns 400")
  @WithMockUser(roles = "ADMIN")
  void returnBook_AlreadyReturned_Returns400() throws Exception {
    // Arrange
    LoanReturnRequestDTO returnDTO = new LoanReturnRequestDTO();
    when(loanService.returnBook(eq(1L), any()))
        .thenThrow(new InvalidLoanOperationException("Loan has already been returned"));

    // Act & Assert
    mockMvc.perform(patch("/api/loans/1/return")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(returnDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("getOverdueLoans - As Admin - Returns 200")
  @WithMockUser(roles = "ADMIN")
  void getOverdueLoans_AsAdmin_Returns200() throws Exception {
    // Arrange
    LoanResponseDTO overdueLoan1 = new LoanResponseDTO();
    overdueLoan1.setId(1L);
    overdueLoan1.setDueDate(LocalDateTime.now().minusDays(5));

    LoanResponseDTO overdueLoan2 = new LoanResponseDTO();
    overdueLoan2.setId(2L);
    overdueLoan2.setDueDate(LocalDateTime.now().minusDays(10));

    List<LoanResponseDTO> overdueLoans = Arrays.asList(overdueLoan1, overdueLoan2);
    when(loanService.getOverdueLoans()).thenReturn(overdueLoans);

    // Act & Assert
    mockMvc.perform(get("/api/loans/overdue"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[1].id", is(2)));
  }

  @Test
  @DisplayName("getOverdueLoans - As Librarian - Returns 200")
  @WithMockUser(roles = "LIBRARIAN")
  void getOverdueLoans_AsLibrarian_Returns200() throws Exception {
    // Arrange
    when(loanService.getOverdueLoans()).thenReturn(Arrays.asList());

    // Act & Assert
    mockMvc.perform(get("/api/loans/overdue"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("getOverdueLoans - As Member - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void getOverdueLoans_AsMember_Returns403() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/loans/overdue"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("getActiveLoans - As Librarian - Returns 200")
  @WithMockUser(roles = "LIBRARIAN")
  void getActiveLoans_AsLibrarian_Returns200() throws Exception {
    // Arrange
    LoanResponseDTO activeLoan1 = new LoanResponseDTO();
    activeLoan1.setId(1L);
    activeLoan1.setReturnDate(null);

    LoanResponseDTO activeLoan2 = new LoanResponseDTO();
    activeLoan2.setId(2L);
    activeLoan2.setReturnDate(null);

    List<LoanResponseDTO> activeLoans = Arrays.asList(activeLoan1, activeLoan2);
    when(loanService.getActiveLoansList()).thenReturn(activeLoans);

    // Act & Assert
    mockMvc.perform(get("/api/loans/active"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[1].id", is(2)));
  }

  @Test
  @DisplayName("getActiveLoans - As Admin - Returns 200")
  @WithMockUser(roles = "ADMIN")
  void getActiveLoans_AsAdmin_Returns200() throws Exception {
    // Arrange
    when(loanService.getActiveLoansList()).thenReturn(Arrays.asList());

    // Act & Assert
    mockMvc.perform(get("/api/loans/active"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("getActiveLoans - As Member - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void getActiveLoans_AsMember_Returns403() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/loans/active"))
        .andExpect(status().isForbidden());
  }
}
