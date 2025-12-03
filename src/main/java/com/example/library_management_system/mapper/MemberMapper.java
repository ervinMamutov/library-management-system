package com.example.library_management_system.mapper;

import com.example.library_management_system.dto.member.MemberCreateRequestDTO;
import com.example.library_management_system.dto.member.MemberResponseDTO;
import com.example.library_management_system.model.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

  public Member toEntity(MemberCreateRequestDTO dto) {
    return new Member(
            dto.getName(),
            dto.getEmail(),
            dto.getPhone()
    );
  }

  public MemberResponseDTO toResponseDTO(Member member) {
    return new MemberResponseDTO(
            member.getId(),
            member.getName(),
            member.getEmail(),
            member.getPhone(),
            member.getMembershipDate()
    );
  }
}
