package com.example.library_management_system.exception;

public class InvalidLoanOperationException extends RuntimeException {

  public InvalidLoanOperationException(String message) {
    super(message);
  }

  public InvalidLoanOperationException(String message, Throwable cause) {
    super(message, cause);
  }
}
