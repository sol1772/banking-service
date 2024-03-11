package com.example.bankingservice.repository;

import com.example.bankingservice.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {
    Email findEmailByContent(String content);
}
