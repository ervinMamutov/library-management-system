package com.example.library_management_system.dto.loan;

import java.time.LocalDateTime;

public class LoanReturnRequestDTO {

  private LocalDateTime returnDate;

  public LoanReturnRequestDTO() {
  }

  public LoanReturnRequestDTO(LocalDateTime returnDate) {
    this.returnDate = returnDate;
  }

  public LocalDateTime getReturnDate() {
    return returnDate;
  }

  public void setReturnDate(LocalDateTime returnDate) {
    this.returnDate = returnDate;
  }
}
