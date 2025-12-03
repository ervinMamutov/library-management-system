package com.example.library_management_system.repository;

import com.example.library_management_system.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

  Optional<Book> findByIsbn(String isbn);

  boolean existsByIsbn(String isbn);
}
