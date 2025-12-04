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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

  private final LoanRepository loanRepository;
  private final LoanMapper loanMapper;
  private final BookRepository bookRepository;
  private final MemberRepository memberRepository;
  private final BookService bookService;

  public LoanService(LoanRepository loanRepository,
                     LoanMapper loanMapper,
                     BookRepository bookRepository,
                     MemberRepository memberRepository,
                     BookService bookService) {
    this.loanRepository = loanRepository;
    this.loanMapper = loanMapper;
    this.bookRepository = bookRepository;
    this.memberRepository = memberRepository;
    this.bookService = bookService;
  }

  @Transactional
  public LoanResponseDTO borrowBook(LoanCreateRequestDTO request) {
    Member member = memberRepository.findById(request.getMemberId())
            .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + request.getMemberId()));

    Book book = bookRepository.findById(request.getBookId())
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + request.getBookId()));

    if (loanRepository.existsByMemberIdAndBookIdAndReturnDateIsNull(request.getMemberId(), request.getBookId())) {
      throw new DuplicateResourceException(
              "Member already has an active loan for this book. Member ID: " + request.getMemberId() + ", Book ID: " + request.getBookId());
    }

    if (book.getCopiesAvailable() <= 0) {
      throw new BookUnavailableException("Book is not available. No copies left. Book ID: " + request.getBookId());
    }

    Loan loan = new Loan();
    loan.setMember(member);
    loan.setBook(book);

    // Set dueDate: use provided date or default to 14 days from now
    LocalDateTime dueDate = request.getDueDate() != null
            ? request.getDueDate()
            : LocalDateTime.now().plusDays(14);
    loan.setDueDate(dueDate);

    Loan savedLoan = loanRepository.save(loan);
    bookService.decrementCopies(request.getBookId());

    return loanMapper.toResponseDTO(savedLoan);
  }

  @Transactional
  public LoanResponseDTO returnBook(Long loanId, LoanReturnRequestDTO request) {
    Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));

    if (loan.getReturnDate() != null) {
      throw new InvalidLoanOperationException("Loan has already been returned. Loan ID: " + loanId);
    }

    LocalDateTime returnDate = request.getReturnDate() != null ? request.getReturnDate() : LocalDateTime.now();
    loan.setReturnDate(returnDate);

    Loan updatedLoan = loanRepository.save(loan);
    bookService.incrementCopies(loan.getBook().getId());

    return loanMapper.toResponseDTO(updatedLoan);
  }

  public List<LoanResponseDTO> getMemberLoanHistory(Long memberId) {
    if (!memberRepository.existsById(memberId)) {
      throw new ResourceNotFoundException("Member not found with id: " + memberId);
    }

    return loanRepository.findByMemberId(memberId)
            .stream()
            .map(loanMapper::toResponseDTO)
            .collect(Collectors.toList());
  }

  public List<LoanResponseDTO> getOverdueLoans() {
    return loanRepository.findByReturnDateIsNullAndDueDateBefore(LocalDateTime.now())
            .stream()
            .map(loanMapper::toResponseDTO)
            .collect(Collectors.toList());
  }

  public List<LoanResponseDTO> getActiveLoansList() {
    return loanRepository.findByReturnDateIsNull()
            .stream()
            .map(loanMapper::toResponseDTO)
            .collect(Collectors.toList());
  }
}
