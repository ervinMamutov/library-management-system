package com.example.library_management_system.controller;

import com.example.library_management_system.dto.loan.LoanResponseDTO;
import com.example.library_management_system.dto.member.MemberCreateRequestDTO;
import com.example.library_management_system.dto.member.MemberResponseDTO;
import com.example.library_management_system.exception.DuplicateResourceException;
import com.example.library_management_system.exception.ResourceNotFoundException;
import com.example.library_management_system.security.JwtAuthenticationFilter;
import com.example.library_management_system.service.MemberService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("MemberController Tests")
class MemberControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private MemberService memberService;

  @MockBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockBean
  private com.example.library_management_system.security.JwtUtil jwtUtil;

  private MemberCreateRequestDTO createRequestDTO;
  private MemberResponseDTO responseDTO;

  @BeforeEach
  void setUp() {
    createRequestDTO = new MemberCreateRequestDTO();
    createRequestDTO.setName("John Doe");
    createRequestDTO.setEmail("john@example.com");
    createRequestDTO.setPhone("+1234567890");

    responseDTO = new MemberResponseDTO();
    responseDTO.setId(1L);
    responseDTO.setName("John Doe");
    responseDTO.setEmail("john@example.com");
    responseDTO.setPhone("+1234567890");
    responseDTO.setMembershipDate(LocalDateTime.now());
  }

  @Test
  @DisplayName("createMember - As Admin - Returns 201")
  @WithMockUser(roles = "ADMIN")
  void createMember_AsAdmin_Returns201() throws Exception {
    // Arrange
    when(memberService.createMember(any(MemberCreateRequestDTO.class))).thenReturn(responseDTO);

    // Act & Assert
    mockMvc.perform(post("/api/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is("John Doe")))
        .andExpect(jsonPath("$.email", is("john@example.com")))
        .andExpect(jsonPath("$.phone", is("+1234567890")));
  }

  @Test
  @DisplayName("createMember - As Librarian - Returns 201")
  @WithMockUser(roles = "LIBRARIAN")
  void createMember_AsLibrarian_Returns201() throws Exception {
    // Arrange
    when(memberService.createMember(any(MemberCreateRequestDTO.class))).thenReturn(responseDTO);

    // Act & Assert
    mockMvc.perform(post("/api/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is("John Doe")));
  }

  // Security test moved to MemberControllerSecurityTest

  @Test
  @DisplayName("createMember - Invalid Request - Returns 400")
  @WithMockUser(roles = "ADMIN")
  void createMember_InvalidRequest_Returns400() throws Exception {
    // Arrange - Create invalid request (missing required fields)
    MemberCreateRequestDTO invalidRequest = new MemberCreateRequestDTO();
    // Missing all required fields

    // Act & Assert
    mockMvc.perform(post("/api/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("createMember - Duplicate Email - Returns 409")
  @WithMockUser(roles = "ADMIN")
  void createMember_DuplicateEmail_Returns409() throws Exception {
    // Arrange
    when(memberService.createMember(any(MemberCreateRequestDTO.class)))
        .thenThrow(new DuplicateResourceException("Member with email already exists"));

    // Act & Assert
    mockMvc.perform(post("/api/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequestDTO)))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("findAllMembers - As Librarian - Returns 200")
  @WithMockUser(roles = "LIBRARIAN")
  void findAllMembers_AsLibrarian_Returns200() throws Exception {
    // Arrange
    MemberResponseDTO member1 = new MemberResponseDTO();
    member1.setId(1L);
    member1.setName("John Doe");

    MemberResponseDTO member2 = new MemberResponseDTO();
    member2.setId(2L);
    member2.setName("Jane Smith");

    List<MemberResponseDTO> members = Arrays.asList(member1, member2);
    when(memberService.findAllMembers()).thenReturn(members);

    // Act & Assert
    mockMvc.perform(get("/api/members"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].name", is("John Doe")))
        .andExpect(jsonPath("$[1].name", is("Jane Smith")));
  }

  @Test
  @DisplayName("findAllMembers - As Admin - Returns 200")
  @WithMockUser(roles = "ADMIN")
  void findAllMembers_AsAdmin_Returns200() throws Exception {
    // Arrange
    when(memberService.findAllMembers()).thenReturn(Arrays.asList());

    // Act & Assert
    mockMvc.perform(get("/api/members"))
        .andExpect(status().isOk());
  }

  // Security test moved to MemberControllerSecurityTest

  @Test
  @DisplayName("findMemberById - As Admin - Returns 200")
  @WithMockUser(roles = "ADMIN")
  void findMemberById_AsAdmin_Returns200() throws Exception {
    // Arrange
    when(memberService.findMemberById(1L)).thenReturn(responseDTO);

    // Act & Assert
    mockMvc.perform(get("/api/members/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is("John Doe")));
  }

  @Test
  @DisplayName("findMemberById - As Librarian - Returns 200")
  @WithMockUser(roles = "LIBRARIAN")
  void findMemberById_AsLibrarian_Returns200() throws Exception {
    // Arrange
    when(memberService.findMemberById(1L)).thenReturn(responseDTO);

    // Act & Assert
    mockMvc.perform(get("/api/members/1"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("findMemberById - Non-Existing Member - Returns 404")
  @WithMockUser(roles = "ADMIN")
  void findMemberById_NonExistingMember_Returns404() throws Exception {
    // Arrange
    when(memberService.findMemberById(999L))
        .thenThrow(new ResourceNotFoundException("Member not found with id: 999"));

    // Act & Assert
    mockMvc.perform(get("/api/members/999"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("deleteMember - As Admin - Returns 204")
  @WithMockUser(roles = "ADMIN")
  void deleteMember_AsAdmin_Returns204() throws Exception {
    // Arrange
    doNothing().when(memberService).deleteMember(1L);

    // Act & Assert
    mockMvc.perform(delete("/api/members/1"))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("deleteMember - As Librarian - Returns 204")
  @WithMockUser(roles = "LIBRARIAN")
  void deleteMember_AsLibrarian_Returns204() throws Exception {
    // Arrange
    doNothing().when(memberService).deleteMember(1L);

    // Act & Assert
    mockMvc.perform(delete("/api/members/1"))
        .andExpect(status().isNoContent());
  }

  // Security test moved to MemberControllerSecurityTest

  @Test
  @DisplayName("deleteMember - Non-Existing Member - Returns 404")
  @WithMockUser(roles = "ADMIN")
  void deleteMember_NonExistingMember_Returns404() throws Exception {
    // Arrange
    doThrow(new ResourceNotFoundException("Member not found with id: 999"))
        .when(memberService).deleteMember(999L);

    // Act & Assert
    mockMvc.perform(delete("/api/members/999"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("getMemberLoanHistory - As Librarian - Returns 200")
  @WithMockUser(roles = "LIBRARIAN")
  void getMemberLoanHistory_AsLibrarian_Returns200() throws Exception {
    // Arrange
    LoanResponseDTO loan1 = new LoanResponseDTO();
    loan1.setId(1L);
    loan1.setMemberId(1L);
    loan1.setBookId(2L);

    LoanResponseDTO loan2 = new LoanResponseDTO();
    loan2.setId(2L);
    loan2.setMemberId(1L);
    loan2.setBookId(3L);

    List<LoanResponseDTO> loanHistory = Arrays.asList(loan1, loan2);
    when(memberService.getMemberLoanHistory(1L)).thenReturn(loanHistory);

    // Act & Assert
    mockMvc.perform(get("/api/members/1/loans"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[1].id", is(2)));
  }

  @Test
  @DisplayName("getMemberLoanHistory - As Member - Returns 200")
  @WithMockUser(roles = "MEMBER")
  void getMemberLoanHistory_AsMember_Returns200() throws Exception {
    // Arrange
    when(memberService.getMemberLoanHistory(1L)).thenReturn(Arrays.asList());

    // Act & Assert
    mockMvc.perform(get("/api/members/1/loans"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("getMemberLoanHistory - As Admin - Returns 200")
  @WithMockUser(roles = "ADMIN")
  void getMemberLoanHistory_AsAdmin_Returns200() throws Exception {
    // Arrange
    when(memberService.getMemberLoanHistory(1L)).thenReturn(Arrays.asList());

    // Act & Assert
    mockMvc.perform(get("/api/members/1/loans"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("getMemberLoanHistory - Non-Existing Member - Returns 404")
  @WithMockUser(roles = "ADMIN")
  void getMemberLoanHistory_NonExistingMember_Returns404() throws Exception {
    // Arrange
    when(memberService.getMemberLoanHistory(999L))
        .thenThrow(new ResourceNotFoundException("Member not found with id: 999"));

    // Act & Assert
    mockMvc.perform(get("/api/members/999/loans"))
        .andExpect(status().isNotFound());
  }
}
