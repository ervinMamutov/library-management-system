package com.example.library_management_system.util;

import com.example.library_management_system.dto.book.BookCreateRequestDTO;
import com.example.library_management_system.dto.book.BookResponseDTO;
import com.example.library_management_system.dto.member.MemberCreateRequestDTO;
import com.example.library_management_system.model.Book;
import com.example.library_management_system.model.Loan;
import com.example.library_management_system.model.Member;
import com.example.library_management_system.model.Role;
import com.example.library_management_system.model.User;

import java.time.LocalDateTime;

/**
 * Test data builder utility for creating test objects
 */
public class TestDataBuilder {

  // Book Test Data
  public static Book createTestBook() {
    return new Book("1984", "George Orwell", "978-0451524935",
                   "Dystopian", 1949, 5);
  }

  public static Book createTestBookWithId(Long id) {
    Book book = createTestBook();
    book.setId(id);
    return book;
  }

  public static Book createTestBookWithISBN(String isbn) {
    return new Book("Test Title", "Test Author", isbn,
                   "Fiction", 2024, 3);
  }

  public static Book createTestBookWithCopies(Integer copies) {
    return new Book("Test Title", "Test Author", "978-1234567890",
                   "Fiction", 2024, copies);
  }

  public static BookCreateRequestDTO createBookCreateRequestDTO() {
    BookCreateRequestDTO dto = new BookCreateRequestDTO();
    dto.setTitle("1984");
    dto.setAuthor("George Orwell");
    dto.setIsbn("978-0451524935");
    dto.setGenre("Dystopian");
    dto.setPublicationYear(1949);
    dto.setCopiesAvailable(5);
    return dto;
  }

  public static BookResponseDTO createBookResponseDTO() {
    return new BookResponseDTO(
        1L, "1984", "George Orwell", "978-0451524935",
        "Dystopian", 1949, 5,
        LocalDateTime.now(), "admin",
        LocalDateTime.now(), "admin"
    );
  }

  // Member Test Data
  public static Member createTestMember() {
    return new Member("John Doe", "john@example.com", "+1234567890");
  }

  public static Member createTestMemberWithId(Long id) {
    Member member = createTestMember();
    member.setId(id);
    return member;
  }

  public static Member createTestMemberWithEmail(String email) {
    return new Member("John Doe", email, "+1234567890");
  }

  public static MemberCreateRequestDTO createMemberCreateRequestDTO() {
    MemberCreateRequestDTO dto = new MemberCreateRequestDTO();
    dto.setName("John Doe");
    dto.setEmail("john@example.com");
    dto.setPhone("+1234567890");
    return dto;
  }

  // User Test Data
  public static User createTestUser(Role role) {
    return new User("testuser", "hashedpassword123", role);
  }

  public static User createTestUserWithId(Long id, Role role) {
    User user = createTestUser(role);
    user.setId(id);
    return user;
  }

  public static User createTestAdmin() {
    return new User("admin", "adminpassword", Role.ADMIN);
  }

  public static User createTestLibrarian() {
    return new User("librarian", "librarianpassword", Role.LIBRARIAN);
  }

  public static User createTestMemberUser() {
    return new User("member", "memberpassword", Role.MEMBER);
  }

  // Loan Test Data
  public static Loan createTestLoan(Member member, Book book) {
    Loan loan = new Loan();
    loan.setMember(member);
    loan.setBook(book);
    loan.setDueDate(LocalDateTime.now().plusDays(14));
    return loan;
  }

  public static Loan createTestLoanWithId(Long id, Member member, Book book) {
    Loan loan = createTestLoan(member, book);
    loan.setId(id);
    return loan;
  }

  public static Loan createTestActiveLoan(Member member, Book book) {
    Loan loan = createTestLoan(member, book);
    loan.setReturnDate(null); // Active loan (not returned)
    return loan;
  }

  public static Loan createTestReturnedLoan(Member member, Book book) {
    Loan loan = createTestLoan(member, book);
    loan.setReturnDate(LocalDateTime.now()); // Returned loan
    return loan;
  }

  public static Loan createTestOverdueLoan(Member member, Book book) {
    Loan loan = createTestLoan(member, book);
    loan.setDueDate(LocalDateTime.now().minusDays(7)); // Overdue by 7 days
    loan.setReturnDate(null); // Not returned yet
    return loan;
  }
}
