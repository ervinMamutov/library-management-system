package com.example.library_management_system.mapper;

import com.example.library_management_system.dto.loan.LoanResponseDTO;
import com.example.library_management_system.model.Loan;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

  public LoanResponseDTO toResponseDTO(Loan loan) {
    return new LoanResponseDTO(
            loan.getId(),
            loan.getMember().getId(),
            loan.getBook().getId(),
            loan.getMember().getName(),
            loan.getBook().getTitle(),
            loan.getBorrowDate(),
            loan.getDueDate(),
            loan.getReturnDate(),
            loan.isOverdue()
    );
  }
}
