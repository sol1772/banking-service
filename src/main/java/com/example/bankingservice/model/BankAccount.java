package com.example.bankingservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class BankAccount implements Serializable {
    @Serial
    private static final long serialVersionUID = 1105122041958251257L;

    @JsonManagedReference(value = "account-fromTransaction")
    @OneToMany(mappedBy = "fromAccount", cascade = CascadeType.ALL)
    private final List<BankTransaction> fromTransactions = new ArrayList<>();

    @JsonManagedReference(value = "account-toTransaction")
    @OneToMany(mappedBy = "toAccount", cascade = CascadeType.ALL)
    private final List<BankTransaction> toTransactions = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number;

    private BigDecimal initialBalance;

    private BigDecimal balance;

    @JsonBackReference(value = "user-account")
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userId")
    private User user;

    public BankAccount(Long id, String number, BigDecimal balance, User user) {
        this.id = id;
        this.number = number;
        this.initialBalance = balance;
        this.balance = balance;
        this.user = user;
    }

    @Override
    public String toString() {
        return number + " {" + user + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankAccount account = (BankAccount) o;
        return Objects.equals(id, account.id) && Objects.equals(user, account.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user);
    }
}
