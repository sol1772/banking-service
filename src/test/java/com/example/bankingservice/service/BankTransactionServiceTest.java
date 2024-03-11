package com.example.bankingservice.service;

import com.example.bankingservice.TestConfig;
import com.example.bankingservice.model.BankAccount;
import com.example.bankingservice.model.User;
import com.example.bankingservice.repository.UserRepository;
import com.example.bankingservice.util.DateUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(scripts = "/truncate_tables.sql", executionPhase = BEFORE_TEST_METHOD)
class BankTransactionServiceTest {
    private static final String DELIMITER = "----------------------------------";
    @Autowired
    private BankTransactionService service;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private DataSource dataSource;

    @BeforeAll
    void init() throws SQLException {
        System.out.println(DELIMITER);
        System.out.println("Test BankTransactionService.beforeAll");
        ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("create_tables.sql"));
    }

    User getNewUser() {
        return new User(null, 1L, "Payer", "payer",
                LocalDate.parse("2003-12-30", DateUtil.DATE_FORMATTER), "", now());
    }

    User getNewTargetUser() {
        return new User(null, 1L, "Receiver", "receiver",
                LocalDate.parse("1997-07-07", DateUtil.DATE_FORMATTER), "", now());
    }

    BankAccount getNewBankAccount(User user) {
        return new BankAccount(1L, "40817123456789", BigDecimal.valueOf(1000.00), user);
    }

    @Test
    void chargeInterest() {
        System.out.println(DELIMITER);
        System.out.println("Test BankTransactionService.chargeInterest()");
        User user = getNewUser();
        BankAccount account = getNewBankAccount(user);
        user.setAccount(account);
        User dbUser = userRepo.save(user);
        assertNotNull(dbUser);
        service.chargeInterest(dbUser.getAccount());
        assertEquals(BigDecimal.valueOf(1050.00).setScale(2, RoundingMode.HALF_EVEN),
                dbUser.getAccount().getBalance().setScale(2, RoundingMode.HALF_EVEN));
        System.out.println("Charged interest for account " + dbUser.getAccount());

    }

    @Test
    void increaseBalance() {
        System.out.println(DELIMITER);
        System.out.println("Test BankTransactionService.increaseBalance()");
        User user = getNewUser();
        BankAccount account = getNewBankAccount(user);
        user.setAccount(account);
        User dbUser = userRepo.save(user);
        assertNotNull(dbUser);
        service.increaseBalance(dbUser.getAccount(), BigDecimal.valueOf(9000.00));
        assertEquals(BigDecimal.valueOf(10000.00).setScale(2, RoundingMode.HALF_EVEN),
                dbUser.getAccount().getBalance().setScale(2, RoundingMode.HALF_EVEN));
        System.out.println("Increased balance of account " + dbUser.getAccount());
    }

    @Test
    void decreaseBalance() {
        System.out.println(DELIMITER);
        System.out.println("Test BankTransactionService.decreaseBalance()");
        User user = getNewUser();
        BankAccount account = getNewBankAccount(user);
        user.setAccount(account);
        User dbUser = userRepo.save(user);
        assertNotNull(dbUser);
        service.decreaseBalance(dbUser.getAccount(), BigDecimal.valueOf(100.00));
        assertEquals(BigDecimal.valueOf(900.00).setScale(2, RoundingMode.HALF_EVEN),
                dbUser.getAccount().getBalance().setScale(2, RoundingMode.HALF_EVEN));
        System.out.println("Decreased balance of account " + dbUser.getAccount());
    }

    @Test
    void decreaseBalanceLessThanZero() {
        System.out.println(DELIMITER);
        System.out.println("Test BankTransactionService.decreaseBalance() (LessThanZero)");
        User user = getNewUser();
        BankAccount account = getNewBankAccount(user);
        user.setAccount(account);
        User dbUser = userRepo.save(user);
        assertNotNull(dbUser);
        service.decreaseBalance(dbUser.getAccount(), BigDecimal.valueOf(1001.00));
        assertEquals(BigDecimal.valueOf(1000.00).setScale(2, RoundingMode.HALF_EVEN),
                dbUser.getAccount().getBalance().setScale(2, RoundingMode.HALF_EVEN));
        System.out.println("Failed decreasing balance of account less than zero: " + dbUser.getAccount());
    }

    @Test
    void internalFundTransfer() {
        System.out.println(DELIMITER);
        System.out.println("Test BankTransactionService.internalFundTransfer()");

        User user = getNewUser();
        BankAccount fromAccount = getNewBankAccount(user);
        user.setAccount(fromAccount);
        User dbUser = userRepo.save(user);
        assertNotNull(dbUser);

        User targetUser = getNewTargetUser();
        BankAccount toAccount = new BankAccount(2L, "40817987654312", BigDecimal.valueOf(100.00), targetUser);
        targetUser.setAccount(toAccount);
        User dbTargetUser = userRepo.save(targetUser);
        assertNotNull(dbTargetUser);

        service.internalFundTransfer(dbUser.getAccount(), dbTargetUser.getAccount(), BigDecimal.valueOf(450.00));
        assertEquals(BigDecimal.valueOf(550.00).setScale(2, RoundingMode.HALF_EVEN),
                dbUser.getAccount().getBalance().setScale(2, RoundingMode.HALF_EVEN));
        assertEquals(BigDecimal.valueOf(550.00).setScale(2, RoundingMode.HALF_EVEN),
                dbTargetUser.getAccount().getBalance().setScale(2, RoundingMode.HALF_EVEN));
        System.out.println("Internal fund transfer from account " + dbUser.getAccount()
                + " to account " + dbTargetUser.getAccount());
    }
}