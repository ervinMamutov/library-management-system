package com.example.library_management_system.repository;

import com.example.library_management_system.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByEmail(String email);

  boolean existsByEmail(String email);
}
