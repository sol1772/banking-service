package com.example.bankingservice.model;

import com.example.bankingservice.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_")
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1905122041950251207L;

    @JsonManagedReference(value = "user-phone")
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @NotEmpty(message = "Should be at least one phone / Должен быть как минимум один телефон")
    private final List<Phone> phones = new ArrayList<>();

    @JsonManagedReference(value = "user-email")
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @NotEmpty(message = "Should be at least one email / Должен быть как минимум один email")
    private final List<Email> emails = new ArrayList<>();

    @JsonManagedReference(value = "user-account")
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private BankAccount account;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @NaturalId
    @Column(name = "login_", unique = true)
    private String login;

    @DateTimeFormat(pattern = DateUtil.DATE_PATTERN)
    private LocalDate dateOfBirth;

    private String passwordHash;

    @CreationTimestamp
    @DateTimeFormat(pattern = DateUtil.DATE_TIME_PATTERN)
    private LocalDateTime registeredAt;

    @XmlTransient
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @XmlTransient
    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String hashPassword(String password) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder.encode(password);
    }

    @Override
    public String toString() {
        return username + " (" + login + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(account, user.account) && Objects.equals(username, user.username)
                && Objects.equals(login, user.login) && Objects.equals(dateOfBirth, user.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, id, username, login, dateOfBirth);
    }
}
