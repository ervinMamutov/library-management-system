package com.example.library_management_system.controller;

import com.example.library_management_system.dto.loan.LoanCreateRequestDTO;
import com.example.library_management_system.dto.loan.LoanResponseDTO;
import com.example.library_management_system.dto.loan.LoanReturnRequestDTO;
import com.example.library_management_system.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

  private final LoanService loanService;

  public LoanController(LoanService loanService) {
    this.loanService = loanService;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
  public ResponseEntity<LoanResponseDTO> borrowBook(@Valid @RequestBody LoanCreateRequestDTO request) {
    LoanResponseDTO loan = loanService.borrowBook(request);
    return new ResponseEntity<>(loan, HttpStatus.CREATED);
  }

  @PatchMapping("/{id}/return")
  @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
  public ResponseEntity<LoanResponseDTO> returnBook(@PathVariable Long id,
                                                      @RequestBody(required = false) LoanReturnRequestDTO request) {
    LoanResponseDTO returned = loanService.returnBook(id, request);
    return ResponseEntity.ok(returned);
  }

  @GetMapping("/overdue")
  @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
  public ResponseEntity<List<LoanResponseDTO>> getOverdueLoans() {
    List<LoanResponseDTO> overdueLoans = loanService.getOverdueLoans();
    return ResponseEntity.ok(overdueLoans);
  }

  @GetMapping("/active")
  @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
  public ResponseEntity<List<LoanResponseDTO>> getActiveLoans() {
    List<LoanResponseDTO> activeLoans = loanService.getActiveLoansList();
    return ResponseEntity.ok(activeLoans);
  }
}
