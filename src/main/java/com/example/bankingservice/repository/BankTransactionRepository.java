package com.example.bankingservice.repository;

import com.example.bankingservice.model.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    Optional<BankTransaction> findByTransactionReference(String transactionReference);
}
