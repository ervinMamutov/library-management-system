package com.example.library_management_system.service;

import com.example.library_management_system.dto.loan.LoanCreateRequestDTO;
import com.example.library_management_system.dto.loan.LoanResponseDTO;
import com.example.library_management_system.dto.loan.LoanReturnRequestDTO;
import com.example.library_management_system.exception.BookUnavailableException;
import com.example.library_management_system.exception.DuplicateResourceException;
import com.example.library_management_system.exception.InvalidLoanOperationException;
import com.example.library_management_system.exception.ResourceNotFoundException;
import com.example.library_management_system.mapper.LoanMapper;
import com.example.library_management_system.model.Book;
import com.example.library_management_system.model.Loan;
import com.example.library_management_system.model.Member;
import com.example.library_management_system.repository.BookRepository;
import com.example.library_management_system.repository.LoanRepository;
import com.example.library_management_system.repository.MemberRepository;
import com.example.library_management_system.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanService Tests")
class LoanServiceTest {

  @Mock
  private LoanRepository loanRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private BookRepository bookRepository;

  @Mock
  private BookService bookService;

  @Mock
  private LoanMapper loanMapper;

  @InjectMocks
  private LoanService loanService;

  private Member testMember;
  private Book testBook;
  private Loan testLoan;
  private LoanCreateRequestDTO createRequestDTO;
  private LoanResponseDTO responseDTO;

  @BeforeEach
  void setUp() {
    testMember = TestDataBuilder.createTestMemberWithId(1L);
    testBook = TestDataBuilder.createTestBookWithCopies(5);
    testBook.setId(2L);

    testLoan = TestDataBuilder.createTestLoan(testMember, testBook);
    testLoan.setId(1L);

    createRequestDTO = new LoanCreateRequestDTO();
    createRequestDTO.setMemberId(1L);
    createRequestDTO.setBookId(2L);
    createRequestDTO.setDueDate(null); // Will default to 14 days

    responseDTO = new LoanResponseDTO();
    responseDTO.setId(1L);
    responseDTO.setMemberId(1L);
    responseDTO.setBookId(2L);
  }

  @Test
  @DisplayName("borrowBook - Valid Request - Creates Loan")
  void borrowBook_ValidRequest_CreatesLoan() {
    // Arrange
    when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
    when(bookRepository.findById(2L)).thenReturn(Optional.of(testBook));
    when(loanRepository.existsByMemberIdAndBookIdAndReturnDateIsNull(1L, 2L)).thenReturn(false);
    when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
    doNothing().when(bookService).decrementCopies(2L);
    when(loanMapper.toResponseDTO(testLoan)).thenReturn(responseDTO);

    // Act
    LoanResponseDTO result = loanService.borrowBook(createRequestDTO);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    verify(memberRepository).findById(1L);
    verify(bookRepository).findById(2L);
    verify(loanRepository).existsByMemberIdAndBookIdAndReturnDateIsNull(1L, 2L);
    verify(loanRepository).save(any(Loan.class));
    verify(bookService).decrementCopies(2L);
  }

  @Test
  @DisplayName("borrowBook - No Copies Available - Throws BookUnavailableException")
  void borrowBook_NoCopiesAvailable_ThrowsBookUnavailableException() {
    // Arrange
    testBook.setCopiesAvailable(0);
    when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
    when(bookRepository.findById(2L)).thenReturn(Optional.of(testBook));
    when(loanRepository.existsByMemberIdAndBookIdAndReturnDateIsNull(1L, 2L)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> loanService.borrowBook(createRequestDTO))
        .isInstanceOf(BookUnavailableException.class)
        .hasMessageContaining("Book is not available");

    verify(loanRepository, never()).save(any());
    verify(bookService, never()).decrementCopies(any());
  }

  @Test
  @DisplayName("borrowBook - Duplicate Loan - Throws DuplicateResourceException")
  void borrowBook_DuplicateLoan_ThrowsDuplicateResourceException() {
    // Arrange
    when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
    when(bookRepository.findById(2L)).thenReturn(Optional.of(testBook));
    when(loanRepository.existsByMemberIdAndBookIdAndReturnDateIsNull(1L, 2L)).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> loanService.borrowBook(createRequestDTO))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("Member already has an active loan for this book");

    verify(loanRepository, never()).save(any());
    verify(bookService, never()).decrementCopies(any());
  }

  @Test
  @DisplayName("borrowBook - Non-Existing Member - Throws ResourceNotFoundException")
  void borrowBook_NonExistingMember_ThrowsResourceNotFoundException() {
    // Arrange
    when(memberRepository.findById(999L)).thenReturn(Optional.empty());
    createRequestDTO.setMemberId(999L);

    // Act & Assert
    assertThatThrownBy(() -> loanService.borrowBook(createRequestDTO))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Member not found with id: 999");

    verify(bookRepository, never()).findById(any());
    verify(loanRepository, never()).save(any());
  }

  @Test
  @DisplayName("borrowBook - Non-Existing Book - Throws ResourceNotFoundException")
  void borrowBook_NonExistingBook_ThrowsResourceNotFoundException() {
    // Arrange
    when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
    when(bookRepository.findById(999L)).thenReturn(Optional.empty());
    createRequestDTO.setBookId(999L);

    // Act & Assert
    assertThatThrownBy(() -> loanService.borrowBook(createRequestDTO))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Book not found with id: 999");

    verify(loanRepository, never()).save(any());
  }

  @Test
  @DisplayName("borrowBook - No DueDate Provided - Defaults To 14 Days")
  void borrowBook_NoDueDateProvided_DefaultsTo14Days() {
    // Arrange
    when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
    when(bookRepository.findById(2L)).thenReturn(Optional.of(testBook));
    when(loanRepository.existsByMemberIdAndBookIdAndReturnDateIsNull(1L, 2L)).thenReturn(false);
    when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
      Loan loan = invocation.getArgument(0);
      // Verify dueDate is set to approximately 14 days from now
      assertThat(loan.getDueDate()).isAfter(LocalDateTime.now().plusDays(13));
      assertThat(loan.getDueDate()).isBefore(LocalDateTime.now().plusDays(15));
      return loan;
    });
    doNothing().when(bookService).decrementCopies(2L);
    when(loanMapper.toResponseDTO(any(Loan.class))).thenReturn(responseDTO);

    // Act
    loanService.borrowBook(createRequestDTO);

    // Assert
    verify(loanRepository).save(any(Loan.class));
  }

  @Test
  @DisplayName("returnBook - Valid Loan - Updates Return Date")
  void returnBook_ValidLoan_UpdatesReturnDate() {
    // Arrange
    testLoan.setReturnDate(null); // Active loan
    LoanReturnRequestDTO returnDTO = new LoanReturnRequestDTO();
    returnDTO.setReturnDate(LocalDateTime.now());

    when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
    when(loanRepository.save(testLoan)).thenReturn(testLoan);
    doNothing().when(bookService).incrementCopies(2L);
    when(loanMapper.toResponseDTO(testLoan)).thenReturn(responseDTO);

    // Act
    LoanResponseDTO result = loanService.returnBook(1L, returnDTO);

    // Assert
    assertThat(result).isNotNull();
    assertThat(testLoan.getReturnDate()).isNotNull();
    verify(loanRepository).findById(1L);
    verify(loanRepository).save(testLoan);
    verify(bookService).incrementCopies(2L);
  }

  @Test
  @DisplayName("returnBook - Already Returned - Throws InvalidLoanOperationException")
  void returnBook_AlreadyReturned_ThrowsInvalidLoanOperationException() {
    // Arrange
    testLoan.setReturnDate(LocalDateTime.now().minusDays(1)); // Already returned
    LoanReturnRequestDTO returnDTO = new LoanReturnRequestDTO();

    when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));

    // Act & Assert
    assertThatThrownBy(() -> loanService.returnBook(1L, returnDTO))
        .isInstanceOf(InvalidLoanOperationException.class)
        .hasMessageContaining("Loan has already been returned");

    verify(loanRepository).findById(1L);
    verify(loanRepository, never()).save(any());
    verify(bookService, never()).incrementCopies(any());
  }

  @Test
  @DisplayName("returnBook - Non-Existing Loan - Throws ResourceNotFoundException")
  void returnBook_NonExistingLoan_ThrowsResourceNotFoundException() {
    // Arrange
    LoanReturnRequestDTO returnDTO = new LoanReturnRequestDTO();
    when(loanRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> loanService.returnBook(999L, returnDTO))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Loan not found with id: 999");

    verify(loanRepository).findById(999L);
    verify(loanRepository, never()).save(any());
  }

  @Test
  @DisplayName("getOverdueLoans - Returns Overdue Loans")
  void getOverdueLoans_ReturnsOverdueLoans() {
    // Arrange
    Loan overdueLoan1 = TestDataBuilder.createTestOverdueLoan(testMember, testBook);
    Loan overdueLoan2 = TestDataBuilder.createTestOverdueLoan(testMember, testBook);
    List<Loan> overdueLoans = Arrays.asList(overdueLoan1, overdueLoan2);

    when(loanRepository.findByReturnDateIsNullAndDueDateBefore(any(LocalDateTime.class)))
        .thenReturn(overdueLoans);
    when(loanMapper.toResponseDTO(any(Loan.class))).thenReturn(responseDTO);

    // Act
    List<LoanResponseDTO> result = loanService.getOverdueLoans();

    // Assert
    assertThat(result).hasSize(2);
    verify(loanRepository).findByReturnDateIsNullAndDueDateBefore(any(LocalDateTime.class));
    verify(loanMapper, times(2)).toResponseDTO(any(Loan.class));
  }

  @Test
  @DisplayName("getActiveLoansList - Returns Active Loans")
  void getActiveLoansList_ReturnsActiveLoans() {
    // Arrange
    Loan activeLoan1 = TestDataBuilder.createTestActiveLoan(testMember, testBook);
    Loan activeLoan2 = TestDataBuilder.createTestActiveLoan(testMember, testBook);
    List<Loan> activeLoans = Arrays.asList(activeLoan1, activeLoan2);

    when(loanRepository.findByReturnDateIsNull()).thenReturn(activeLoans);
    when(loanMapper.toResponseDTO(any(Loan.class))).thenReturn(responseDTO);

    // Act
    List<LoanResponseDTO> result = loanService.getActiveLoansList();

    // Assert
    assertThat(result).hasSize(2);
    verify(loanRepository).findByReturnDateIsNull();
    verify(loanMapper, times(2)).toResponseDTO(any(Loan.class));
  }

  @Test
  @DisplayName("getMemberLoanHistory - Valid MemberId - Returns History")
  void getMemberLoanHistory_ValidMemberId_ReturnsHistory() {
    // Arrange
    Loan loan1 = TestDataBuilder.createTestLoanWithId(1L, testMember, testBook);
    Loan loan2 = TestDataBuilder.createTestLoanWithId(2L, testMember, testBook);
    List<Loan> loanHistory = Arrays.asList(loan1, loan2);

    when(memberRepository.existsById(1L)).thenReturn(true);
    when(loanRepository.findByMemberId(1L)).thenReturn(loanHistory);
    when(loanMapper.toResponseDTO(any(Loan.class))).thenReturn(responseDTO);

    // Act
    List<LoanResponseDTO> result = loanService.getMemberLoanHistory(1L);

    // Assert
    assertThat(result).hasSize(2);
    verify(memberRepository).existsById(1L);
    verify(loanRepository).findByMemberId(1L);
    verify(loanMapper, times(2)).toResponseDTO(any(Loan.class));
  }

  @Test
  @DisplayName("getMemberLoanHistory - Non-Existing Member - Throws ResourceNotFoundException")
  void getMemberLoanHistory_NonExistingMember_ThrowsResourceNotFoundException() {
    // Arrange
    when(memberRepository.existsById(999L)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> loanService.getMemberLoanHistory(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Member not found with id: 999");

    verify(memberRepository).existsById(999L);
    verify(loanRepository, never()).findByMemberId(any());
  }
}
