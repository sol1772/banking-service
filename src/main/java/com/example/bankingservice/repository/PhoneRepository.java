package com.example.bankingservice.repository;

import com.example.bankingservice.model.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneRepository extends JpaRepository<Phone, Long> {
    Phone findPhoneByNumber(String content);
}
