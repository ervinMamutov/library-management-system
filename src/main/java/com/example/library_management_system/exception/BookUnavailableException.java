package com.example.library_management_system.exception;

public class BookUnavailableException extends RuntimeException {

  public BookUnavailableException(String message) {
    super(message);
  }

  public BookUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
