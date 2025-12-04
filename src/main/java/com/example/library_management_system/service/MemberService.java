package com.example.library_management_system.service;

import com.example.library_management_system.dto.loan.LoanResponseDTO;
import com.example.library_management_system.dto.member.MemberCreateRequestDTO;
import com.example.library_management_system.dto.member.MemberResponseDTO;
import com.example.library_management_system.exception.DuplicateResourceException;
import com.example.library_management_system.exception.InvalidLoanOperationException;
import com.example.library_management_system.exception.ResourceNotFoundException;
import com.example.library_management_system.mapper.LoanMapper;
import com.example.library_management_system.mapper.MemberMapper;
import com.example.library_management_system.model.Member;
import com.example.library_management_system.repository.LoanRepository;
import com.example.library_management_system.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberService {

  private final MemberRepository memberRepository;
  private final MemberMapper memberMapper;
  private final LoanRepository loanRepository;
  private final LoanMapper loanMapper;

  public MemberService(MemberRepository memberRepository,
                       MemberMapper memberMapper,
                       LoanRepository loanRepository,
                       LoanMapper loanMapper) {
    this.memberRepository = memberRepository;
    this.memberMapper = memberMapper;
    this.loanRepository = loanRepository;
    this.loanMapper = loanMapper;
  }

  public MemberResponseDTO createMember(MemberCreateRequestDTO request) {
    if (memberRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("Member with email already exists: " + request.getEmail());
    }

    Member member = memberMapper.toEntity(request);
    Member savedMember = memberRepository.save(member);

    return memberMapper.toResponseDTO(savedMember);
  }

  public List<MemberResponseDTO> findAllMembers() {
    return memberRepository.findAll()
            .stream()
            .map(memberMapper::toResponseDTO)
            .collect(Collectors.toList());
  }

  public MemberResponseDTO findMemberById(Long id) {
    return memberRepository.findById(id)
            .map(memberMapper::toResponseDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
  }

  public void deleteMember(Long id) {
    Member member = memberRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

    List<?> activeLoans = loanRepository.findByMemberIdAndReturnDateIsNull(id);
    if (!activeLoans.isEmpty()) {
      throw new InvalidLoanOperationException(
              "Cannot delete member with active loans. Member has " + activeLoans.size() + " active loan(s)");
    }

    memberRepository.delete(member);
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
}
