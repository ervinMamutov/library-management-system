package com.example.library_management_system.service;

import com.example.library_management_system.dto.loan.LoanResponseDTO;
import com.example.library_management_system.dto.member.MemberCreateRequestDTO;
import com.example.library_management_system.dto.member.MemberResponseDTO;
import com.example.library_management_system.exception.DuplicateResourceException;
import com.example.library_management_system.exception.InvalidLoanOperationException;
import com.example.library_management_system.exception.ResourceNotFoundException;
import com.example.library_management_system.mapper.LoanMapper;
import com.example.library_management_system.mapper.MemberMapper;
import com.example.library_management_system.model.Book;
import com.example.library_management_system.model.Loan;
import com.example.library_management_system.model.Member;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService Tests")
class MemberServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private MemberMapper memberMapper;

  @Mock
  private LoanRepository loanRepository;

  @Mock
  private LoanMapper loanMapper;

  @InjectMocks
  private MemberService memberService;

  private Member testMember;
  private MemberCreateRequestDTO createRequestDTO;
  private MemberResponseDTO responseDTO;

  @BeforeEach
  void setUp() {
    testMember = TestDataBuilder.createTestMemberWithId(1L);

    createRequestDTO = new MemberCreateRequestDTO();
    createRequestDTO.setName("John Doe");
    createRequestDTO.setEmail("john@example.com");
    createRequestDTO.setPhone("+1234567890");

    responseDTO = new MemberResponseDTO();
    responseDTO.setId(1L);
    responseDTO.setName("John Doe");
    responseDTO.setEmail("john@example.com");
    responseDTO.setPhone("+1234567890");
  }

  @Test
  @DisplayName("createMember - Valid Request - Returns Created Member")
  void createMember_ValidRequest_ReturnsCreatedMember() {
    // Arrange
    when(memberRepository.existsByEmail(createRequestDTO.getEmail())).thenReturn(false);
    when(memberMapper.toEntity(createRequestDTO)).thenReturn(testMember);
    when(memberRepository.save(testMember)).thenReturn(testMember);
    when(memberMapper.toResponseDTO(testMember)).thenReturn(responseDTO);

    // Act
    MemberResponseDTO result = memberService.createMember(createRequestDTO);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("John Doe");
    assertThat(result.getEmail()).isEqualTo("john@example.com");
    verify(memberRepository).existsByEmail("john@example.com");
    verify(memberRepository).save(testMember);
    verify(memberMapper).toEntity(createRequestDTO);
    verify(memberMapper).toResponseDTO(testMember);
  }

  @Test
  @DisplayName("createMember - Duplicate Email - Throws DuplicateResourceException")
  void createMember_DuplicateEmail_ThrowsDuplicateResourceException() {
    // Arrange
    when(memberRepository.existsByEmail(createRequestDTO.getEmail())).thenReturn(true);

    // Act & Assert
    assertThatThrownBy(() -> memberService.createMember(createRequestDTO))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("Member with email already exists: john@example.com");

    verify(memberRepository).existsByEmail("john@example.com");
    verify(memberRepository, never()).save(any());
  }

  @Test
  @DisplayName("findAllMembers - Returns All Members")
  void findAllMembers_ReturnsAllMembers() {
    // Arrange
    Member member1 = TestDataBuilder.createTestMemberWithId(1L);
    Member member2 = TestDataBuilder.createTestMemberWithId(2L);
    Member member3 = TestDataBuilder.createTestMemberWithId(3L);
    List<Member> members = Arrays.asList(member1, member2, member3);

    MemberResponseDTO dto1 = new MemberResponseDTO();
    dto1.setId(1L);
    MemberResponseDTO dto2 = new MemberResponseDTO();
    dto2.setId(2L);
    MemberResponseDTO dto3 = new MemberResponseDTO();
    dto3.setId(3L);

    when(memberRepository.findAll()).thenReturn(members);
    when(memberMapper.toResponseDTO(member1)).thenReturn(dto1);
    when(memberMapper.toResponseDTO(member2)).thenReturn(dto2);
    when(memberMapper.toResponseDTO(member3)).thenReturn(dto3);

    // Act
    List<MemberResponseDTO> result = memberService.findAllMembers();

    // Assert
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getId()).isEqualTo(1L);
    assertThat(result.get(1).getId()).isEqualTo(2L);
    assertThat(result.get(2).getId()).isEqualTo(3L);
    verify(memberRepository).findAll();
    verify(memberMapper, times(3)).toResponseDTO(any(Member.class));
  }

  @Test
  @DisplayName("findMemberById - Existing ID - Returns Member")
  void findMemberById_ExistingId_ReturnsMember() {
    // Arrange
    when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
    when(memberMapper.toResponseDTO(testMember)).thenReturn(responseDTO);

    // Act
    MemberResponseDTO result = memberService.findMemberById(1L);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("John Doe");
    verify(memberRepository).findById(1L);
    verify(memberMapper).toResponseDTO(testMember);
  }

  @Test
  @DisplayName("findMemberById - Non-Existing ID - Throws ResourceNotFoundException")
  void findMemberById_NonExistingId_ThrowsResourceNotFoundException() {
    // Arrange
    when(memberRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> memberService.findMemberById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Member not found with id: 999");

    verify(memberRepository).findById(999L);
    verify(memberMapper, never()).toResponseDTO(any());
  }

  @Test
  @DisplayName("deleteMember - Existing ID With No Active Loans - Deletes Member")
  void deleteMember_ExistingIdWithNoActiveLoans_DeletesMember() {
    // Arrange
    when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
    when(loanRepository.findByMemberIdAndReturnDateIsNull(1L)).thenReturn(Collections.emptyList());
    doNothing().when(memberRepository).delete(testMember);

    // Act
    memberService.deleteMember(1L);

    // Assert
    verify(memberRepository).findById(1L);
    verify(loanRepository).findByMemberIdAndReturnDateIsNull(1L);
    verify(memberRepository).delete(testMember);
  }

  @Test
  @DisplayName("deleteMember - Non-Existing ID - Throws ResourceNotFoundException")
  void deleteMember_NonExistingId_ThrowsResourceNotFoundException() {
    // Arrange
    when(memberRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> memberService.deleteMember(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Member not found with id: 999");

    verify(memberRepository).findById(999L);
    verify(memberRepository, never()).delete(any());
  }

  @Test
  @DisplayName("deleteMember - Member With Active Loans - Throws InvalidLoanOperationException")
  void deleteMember_MemberWithActiveLoans_ThrowsInvalidLoanOperationException() {
    // Arrange
    Book testBook = TestDataBuilder.createTestBook();
    Loan activeLoan1 = TestDataBuilder.createTestActiveLoan(testMember, testBook);
    Loan activeLoan2 = TestDataBuilder.createTestActiveLoan(testMember, testBook);
    List<Loan> activeLoans = Arrays.asList(activeLoan1, activeLoan2);

    when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
    when(loanRepository.findByMemberIdAndReturnDateIsNull(1L)).thenReturn(activeLoans);

    // Act & Assert
    assertThatThrownBy(() -> memberService.deleteMember(1L))
        .isInstanceOf(InvalidLoanOperationException.class)
        .hasMessageContaining("Cannot delete member with active loans")
        .hasMessageContaining("2 active loan(s)");

    verify(memberRepository).findById(1L);
    verify(loanRepository).findByMemberIdAndReturnDateIsNull(1L);
    verify(memberRepository, never()).delete(any());
  }

  @Test
  @DisplayName("getMemberLoanHistory - Valid MemberId - Returns History")
  void getMemberLoanHistory_ValidMemberId_ReturnsHistory() {
    // Arrange
    Book testBook = TestDataBuilder.createTestBook();
    Loan loan1 = TestDataBuilder.createTestLoanWithId(1L, testMember, testBook);
    Loan loan2 = TestDataBuilder.createTestLoanWithId(2L, testMember, testBook);
    Loan loan3 = TestDataBuilder.createTestLoanWithId(3L, testMember, testBook);
    List<Loan> loanHistory = Arrays.asList(loan1, loan2, loan3);

    LoanResponseDTO loanDTO1 = new LoanResponseDTO();
    loanDTO1.setId(1L);
    LoanResponseDTO loanDTO2 = new LoanResponseDTO();
    loanDTO2.setId(2L);
    LoanResponseDTO loanDTO3 = new LoanResponseDTO();
    loanDTO3.setId(3L);

    when(memberRepository.existsById(1L)).thenReturn(true);
    when(loanRepository.findByMemberId(1L)).thenReturn(loanHistory);
    when(loanMapper.toResponseDTO(loan1)).thenReturn(loanDTO1);
    when(loanMapper.toResponseDTO(loan2)).thenReturn(loanDTO2);
    when(loanMapper.toResponseDTO(loan3)).thenReturn(loanDTO3);

    // Act
    List<LoanResponseDTO> result = memberService.getMemberLoanHistory(1L);

    // Assert
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getId()).isEqualTo(1L);
    assertThat(result.get(1).getId()).isEqualTo(2L);
    assertThat(result.get(2).getId()).isEqualTo(3L);
    verify(memberRepository).existsById(1L);
    verify(loanRepository).findByMemberId(1L);
    verify(loanMapper, times(3)).toResponseDTO(any(Loan.class));
  }

  @Test
  @DisplayName("getMemberLoanHistory - Non-Existing Member - Throws ResourceNotFoundException")
  void getMemberLoanHistory_NonExistingMember_ThrowsResourceNotFoundException() {
    // Arrange
    when(memberRepository.existsById(999L)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> memberService.getMemberLoanHistory(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Member not found with id: 999");

    verify(memberRepository).existsById(999L);
    verify(loanRepository, never()).findByMemberId(any());
  }
}
