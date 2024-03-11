package com.example.bankingservice.util;

import com.example.bankingservice.model.BankAccount;
import com.example.bankingservice.service.BankTransactionService;
import com.example.bankingservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduledTasks {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private final UserService userService;
    private final BankTransactionService bankTransactionService;

    public ScheduledTasks(UserService userService, BankTransactionService bankTransactionService) {
        this.userService = userService;
        this.bankTransactionService = bankTransactionService;
    }

    @Scheduled(fixedRate = 60 * 1000)
    public void chargeInterestTask() {
        if (logger.isInfoEnabled()) {
            logger.info("Start of the task 'ChargeInterest' / Запуск регламентного задания 'Начисление процентов'");
        }
        List<BankAccount> accounts = userService.getAllBankAccounts();
        for (BankAccount account : accounts) {
            bankTransactionService.chargeInterest(account);
        }
    }
}