package com.example.library_management_system.repository;

import com.example.library_management_system.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

  List<Loan> findByMemberId(Long memberId);

  List<Loan> findByMemberIdAndReturnDateIsNull(Long memberId);

  Optional<Loan> findByMemberIdAndBookIdAndReturnDateIsNull(Long memberId, Long bookId);

  List<Loan> findByReturnDateIsNullAndDueDateBefore(LocalDateTime date);

  boolean existsByMemberIdAndBookIdAndReturnDateIsNull(Long memberId, Long bookId);
}
