package com.example.bankingservice.service;

import com.example.bankingservice.model.BankAccount;
import com.example.bankingservice.model.BankTransaction;
import com.example.bankingservice.model.TransactionStatus;
import com.example.bankingservice.repository.BankAccountRepository;
import com.example.bankingservice.repository.BankTransactionRepository;
import com.example.bankingservice.util.AppRuntimeException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Getter
@Transactional(readOnly = true)
public class BankTransactionService {
    private static final Logger logger = LoggerFactory.getLogger(BankTransactionService.class);
    private final BigDecimal BALANCE_MULTIPLY_LIMIT = BigDecimal.valueOf(2.07);
    private final BigDecimal BALANCE_INCREASE_PERCENTAGE = BigDecimal.valueOf(0.05);
    private final BankTransactionRepository bankTransactionRepository;
    private final BankAccountRepository bankAccountRepository;

    public BankTransactionService(BankTransactionRepository bankTransactionRepository, BankAccountRepository bankAccountRepository) {
        this.bankTransactionRepository = bankTransactionRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<BankTransaction> getAllBankTransactions() {
        return bankTransactionRepository.findAll();
    }

    public Optional<BankTransaction> getByTransactionId(Long id) {
        return bankTransactionRepository.findById(id);
    }

    public Optional<BankTransaction> getByTransactionReference(String transactionReference) {
        return bankTransactionRepository.findByTransactionReference(transactionReference);
    }

    @Transactional
    public void chargeInterest(BankAccount account) {
        final BigDecimal amountLimit = account.getInitialBalance().multiply(BALANCE_MULTIPLY_LIMIT);
        BigDecimal amount = account.getBalance().multiply(BALANCE_INCREASE_PERCENTAGE);
        BigDecimal increasedAmount = account.getBalance().add(amount);
        if ((account.getBalance().compareTo(BigDecimal.ZERO) > 0) && (increasedAmount.compareTo(amountLimit) <= 0)) {
            String transactionId = UUID.randomUUID().toString();
            BankTransaction transaction = new BankTransaction(transactionId, null, account, amount, TransactionStatus.PENDING);
            try {
                transaction.deposit(amount);
                bankAccountRepository.save(account);
                bankTransactionRepository.save(transaction);
                transaction.setStatus(TransactionStatus.SUCCESS);
                if (logger.isInfoEnabled()) {
                    logger.info("The account has accrued interest on the amount / На счет начислены проценты на сумму {} ({})",
                            amount, account);
                    logger.info("Balance after depositing / Баланс после пополнения: " + account.getBalance());
                }
            } catch (AppRuntimeException e) {
                transaction.setStatus(TransactionStatus.FAILED);
                if (logger.isErrorEnabled()) {
                    logger.info("Transaction error / Ошибка транзакции {}", transaction);
                }
            }
        }
    }

    @Transactional
    public void increaseBalance(BankAccount account, BigDecimal amount) {
        String transactionId = UUID.randomUUID().toString();
        BankTransaction transaction = new BankTransaction(transactionId, null, account, amount, TransactionStatus.PENDING);
        try {
            transaction.deposit(amount);
            bankAccountRepository.save(account);
            bankTransactionRepository.save(transaction);
            transaction.setStatus(TransactionStatus.SUCCESS);
            if (logger.isInfoEnabled()) {
                logger.info("Depositing account / Пополнение счета: {} {}", account, amount);
                logger.info("Balance after depositing / Баланс после пополнения: " + account.getBalance());
            }
        } catch (AppRuntimeException e) {
            transaction.setStatus(TransactionStatus.FAILED);
            if (logger.isErrorEnabled()) {
                logger.info("Transaction error / Ошибка транзакции {}", transaction);
            }
        }
    }

    @Transactional
    public void decreaseBalance(BankAccount account, BigDecimal amount) {
        String transactionId = UUID.randomUUID().toString();
        BankTransaction transaction = new BankTransaction(transactionId, account, null, amount, TransactionStatus.PENDING);
        try {
            transaction.withdraw(amount);
            bankAccountRepository.save(account);
            bankTransactionRepository.save(transaction);
            transaction.setStatus(TransactionStatus.SUCCESS);
            if (logger.isInfoEnabled()) {
                logger.info("Withdrawal from account / Снятие со счета: {} {}", account, amount);
                logger.info("Balance after withdrawal / Баланс после снятия: " + account.getBalance());
            }
        } catch (AppRuntimeException e) {
            transaction.setStatus(TransactionStatus.FAILED);
            if (logger.isErrorEnabled()) {
                logger.info("Transaction error / Ошибка транзакции {}", transaction);
            }
        }
    }

    @Transactional
    public void internalFundTransfer(BankAccount fromAccount, BankAccount toAccount, BigDecimal amount) {
        decreaseBalance(fromAccount, amount);
        increaseBalance(toAccount, amount);
    }
}
