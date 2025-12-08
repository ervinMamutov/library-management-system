package com.example.library_management_system.security;

import com.example.library_management_system.dto.book.BookCreateRequestDTO;
import com.example.library_management_system.dto.book.BookImportDTO;
import com.example.library_management_system.dto.book.BookUpdateRequestDTO;
import com.example.library_management_system.dto.loan.LoanCreateRequestDTO;
import com.example.library_management_system.dto.member.MemberCreateRequestDTO;
import com.example.library_management_system.dto.user.UserUpdateRequestDTO;
import com.example.library_management_system.model.Book;
import com.example.library_management_system.model.Member;
import com.example.library_management_system.model.Role;
import com.example.library_management_system.model.User;
import com.example.library_management_system.repository.BookRepository;
import com.example.library_management_system.repository.MemberRepository;
import com.example.library_management_system.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security Integration Tests - Tests role-based access control with Spring Security enabled
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@DisplayName("Security Integration Tests")
class SecurityIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private BookCreateRequestDTO bookCreateDTO;
  private BookUpdateRequestDTO bookUpdateDTO;
  private MemberCreateRequestDTO memberCreateDTO;
  private UserUpdateRequestDTO userUpdateDTO;
  private LoanCreateRequestDTO loanCreateDTO;

  private Book testBook;
  private Member testMember;
  private User adminUser;

  @BeforeEach
  void setUp() {
    // Create test users
    adminUser = new User();
    adminUser.setUsername("admin");
    adminUser.setHashedPassword(passwordEncoder.encode("password"));
    adminUser.setRole(Role.ADMIN);
    userRepository.save(adminUser);

    User librarianUser = new User();
    librarianUser.setUsername("librarian");
    librarianUser.setHashedPassword(passwordEncoder.encode("password"));
    librarianUser.setRole(Role.LIBRARIAN);
    userRepository.save(librarianUser);

    User memberUser = new User();
    memberUser.setUsername("member");
    memberUser.setHashedPassword(passwordEncoder.encode("password"));
    memberUser.setRole(Role.MEMBER);
    userRepository.save(memberUser);

    // Create test book
    testBook = new Book();
    testBook.setTitle("Test Book");
    testBook.setAuthor("Test Author");
    testBook.setIsbn("978-1234567890");
    testBook.setGenre("Fiction");
    testBook.setPublicationYear(2024);
    testBook.setCopiesAvailable(5);
    bookRepository.save(testBook);

    // Create test member
    testMember = new Member();
    testMember.setName("Test Member");
    testMember.setEmail("test@example.com");
    testMember.setPhone("+1234567890");
    testMember.setMembershipDate(LocalDateTime.now());
    memberRepository.save(testMember);
    bookCreateDTO = new BookCreateRequestDTO();
    bookCreateDTO.setTitle("Test Book");
    bookCreateDTO.setAuthor("Test Author");
    bookCreateDTO.setIsbn("978-1234567890");
    bookCreateDTO.setGenre("Fiction");
    bookCreateDTO.setPublicationYear(2024);
    bookCreateDTO.setCopiesAvailable(5);

    bookUpdateDTO = new BookUpdateRequestDTO();
    bookUpdateDTO.setTitle("Updated Title");
    bookUpdateDTO.setAuthor("Updated Author");
    bookUpdateDTO.setGenre("Fiction");
    bookUpdateDTO.setPublicationYear(2024);
    bookUpdateDTO.setCopiesAvailable(5);

    memberCreateDTO = new MemberCreateRequestDTO();
    memberCreateDTO.setName("Test Member");
    memberCreateDTO.setEmail("test@example.com");
    memberCreateDTO.setPhone("+1234567890");

    userUpdateDTO = new UserUpdateRequestDTO();
    userUpdateDTO.setUsername("updateduser");
    userUpdateDTO.setRole("LIBRARIAN");

    loanCreateDTO = new LoanCreateRequestDTO();
    loanCreateDTO.setMemberId(testMember.getId());
    loanCreateDTO.setBookId(testBook.getId());
    loanCreateDTO.setDueDate(LocalDateTime.now().plusDays(14));
  }

  // ==================== Book Endpoint Security Tests ====================

  @Test
  @DisplayName("POST /api/books - MEMBER role - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void createBook_AsMember_Returns403() throws Exception {
    mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(bookCreateDTO)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("POST /api/books - Unauthenticated - Returns 403")
  void createBook_Unauthenticated_Returns403() throws Exception {
    // Spring Security returns 403 for anonymous users
    mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(bookCreateDTO)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /api/books - Unauthenticated - Returns 403")
  void findAllBooks_Unauthenticated_Returns403() throws Exception {
    // Spring Security returns 403 for anonymous users
    mockMvc.perform(get("/api/books"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("PUT /api/books/{id} - MEMBER role - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void updateBook_AsMember_Returns403() throws Exception {
    mockMvc.perform(put("/api/books/" + testBook.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(bookUpdateDTO)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("DELETE /api/books/{id} - MEMBER role - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void deleteBook_AsMember_Returns403() throws Exception {
    mockMvc.perform(delete("/api/books/" + testBook.getId()))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("POST /api/books/import - MEMBER role - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void importBooks_AsMember_Returns403() throws Exception {
    BookImportDTO importDTO = new BookImportDTO();
    importDTO.setIsbn("978-1111111111");
    importDTO.setTitle("Book 1");
    importDTO.setAuthor("Author 1");
    importDTO.setGenre("Genre 1");
    importDTO.setPublicationYear(2020);
    importDTO.setCopies(3);

    List<BookImportDTO> importList = Arrays.asList(importDTO);

    mockMvc.perform(post("/api/books/import")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(importList)))
        .andExpect(status().isForbidden());
  }

  // ==================== Loan Endpoint Security Tests ====================

  @Test
  @DisplayName("POST /api/loans - MEMBER role - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void borrowBook_AsMember_Returns403() throws Exception {
    mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loanCreateDTO)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("PATCH /api/loans/{id}/return - MEMBER role - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void returnBook_AsMember_Returns403() throws Exception {
    // Use arbitrary loan ID - should get 403 before checking if loan exists
    mockMvc.perform(patch("/api/loans/999/return")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /api/loans/overdue - MEMBER role - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void getOverdueLoans_AsMember_Returns403() throws Exception {
    mockMvc.perform(get("/api/loans/overdue"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /api/loans/active - MEMBER role - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void getActiveLoans_AsMember_Returns403() throws Exception {
    mockMvc.perform(get("/api/loans/active"))
        .andExpect(status().isForbidden());
  }

  // ==================== Member Endpoint Security Tests ====================

  @Test
  @DisplayName("POST /api/members - MEMBER role - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void createMember_AsMember_Returns403() throws Exception {
    mockMvc.perform(post("/api/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(memberCreateDTO)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /api/members - MEMBER role - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void findAllMembers_AsMember_Returns403() throws Exception {
    mockMvc.perform(get("/api/members"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("DELETE /api/members/{id} - MEMBER role - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void deleteMember_AsMember_Returns403() throws Exception {
    mockMvc.perform(delete("/api/members/" + testMember.getId()))
        .andExpect(status().isForbidden());
  }

  // ==================== User Endpoint Security Tests ====================

  @Test
  @DisplayName("GET /api/users - LIBRARIAN role - Returns 403")
  @WithMockUser(roles = "LIBRARIAN")
  void findAllUsers_AsLibrarian_Returns403() throws Exception {
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /api/users - MEMBER role - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void findAllUsers_AsMember_Returns403() throws Exception {
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /api/users/{id} - LIBRARIAN role - Returns 403")
  @WithMockUser(roles = "LIBRARIAN")
  void findUserById_AsLibrarian_Returns403() throws Exception {
    mockMvc.perform(get("/api/users/" + adminUser.getId()))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("PUT /api/users/{id} - MEMBER role - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void updateUser_AsMember_Returns403() throws Exception {
    mockMvc.perform(put("/api/users/" + adminUser.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userUpdateDTO)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("PATCH /api/users/{id}/role - LIBRARIAN role - Returns 403")
  @WithMockUser(roles = "LIBRARIAN")
  void updateRole_AsLibrarian_Returns403() throws Exception {
    Map<String, String> roleRequest = new HashMap<>();
    roleRequest.put("role", "ADMIN");

    mockMvc.perform(patch("/api/users/" + adminUser.getId() + "/role")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleRequest)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("DELETE /api/users/{id} - MEMBER role - Returns 403")
  @WithMockUser(roles = "MEMBER")
  void deleteUser_AsMember_Returns403() throws Exception {
    mockMvc.perform(delete("/api/users/" + adminUser.getId()))
        .andExpect(status().isForbidden());
  }

  // ==================== Authentication Tests ====================

  @Test
  @DisplayName("Access protected endpoint without authentication - Returns 403")
  void accessProtectedEndpoint_WithoutAuth_Returns403() throws Exception {
    // Spring Security returns 403 for anonymous users trying to access protected resources
    mockMvc.perform(get("/api/books"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Access admin endpoint as ADMIN - Returns 200 or other success")
  @WithMockUser(roles = "ADMIN")
  void accessAdminEndpoint_AsAdmin_ReturnsSuccess() throws Exception {
    // Should not return 403 - may return 404, 400, or 200 depending on data
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Access librarian endpoint as LIBRARIAN - Returns success")
  @WithMockUser(roles = "LIBRARIAN")
  void accessLibrarianEndpoint_AsLibrarian_ReturnsSuccess() throws Exception {
    mockMvc.perform(get("/api/members"))
        .andExpect(status().isOk());
  }
}
