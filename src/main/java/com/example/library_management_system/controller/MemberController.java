package com.example.library_management_system.controller;

import com.example.library_management_system.dto.loan.LoanResponseDTO;
import com.example.library_management_system.dto.member.MemberCreateRequestDTO;
import com.example.library_management_system.dto.member.MemberResponseDTO;
import com.example.library_management_system.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

  private final MemberService memberService;

  public MemberController(MemberService memberService) {
    this.memberService = memberService;
  }

  @PostMapping
  public ResponseEntity<MemberResponseDTO> createMember(@Valid @RequestBody MemberCreateRequestDTO request) {
    MemberResponseDTO created = memberService.createMember(request);
    return new ResponseEntity<>(created, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<List<MemberResponseDTO>> findAllMembers() {
    List<MemberResponseDTO> members = memberService.findAllMembers();
    return ResponseEntity.ok(members);
  }

  @GetMapping("/{id}")
  public ResponseEntity<MemberResponseDTO> findMemberById(@PathVariable Long id) {
    MemberResponseDTO member = memberService.findMemberById(id);
    return ResponseEntity.ok(member);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
    memberService.deleteMember(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/loans")
  public ResponseEntity<List<LoanResponseDTO>> getMemberLoanHistory(@PathVariable Long id) {
    List<LoanResponseDTO> loans = memberService.getMemberLoanHistory(id);
    return ResponseEntity.ok(loans);
  }
}
