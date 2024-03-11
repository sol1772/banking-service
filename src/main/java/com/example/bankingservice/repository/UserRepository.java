package com.example.bankingservice.repository;

import com.example.bankingservice.model.User;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"phones"})
    Optional<User> findById(@Nonnull Long id);

    @EntityGraph(attributePaths = {"phones"})
    User findUserById(@Param("id") Long id);

    User findUserByLogin(String login);

    @Query(value = "SELECT t.user FROM Phone t WHERE t.number LIKE :num")
    Optional<User> findUserByPhone(@Param("num") String number);

    @Query(value = "SELECT t.user FROM Email t WHERE t.content LIKE :con")
    Optional<User> findUserByEmail(@Param("con") String email);

    Page<User> findByUsernameLike(String username, Pageable pageable);

    Page<User> findByDateOfBirthAfter(LocalDate dateOfBirth, Pageable pageable);
}
