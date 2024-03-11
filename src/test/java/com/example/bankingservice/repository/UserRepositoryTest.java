package com.example.bankingservice.repository;

import com.example.bankingservice.TestConfig;
import com.example.bankingservice.model.Email;
import com.example.bankingservice.model.Phone;
import com.example.bankingservice.model.User;
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
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(scripts = "/truncate_tables.sql", executionPhase = BEFORE_TEST_METHOD)
class UserRepositoryTest {
    private static final String DELIMITER = "----------------------------------";
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private DataSource dataSource;

    @BeforeAll
    void init() throws SQLException {
        System.out.println(DELIMITER);
        System.out.println("Test UserRepository.beforeAll");
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

    @Test
    void save() {
        System.out.println(DELIMITER);
        System.out.println("Test UserRepository.save()");
        User dbUser = userRepo.save(getNewUser());
        assertNotNull(dbUser);
        System.out.println("Saved " + dbUser);
    }

    @Test
    void findById() {
        System.out.println(DELIMITER);
        System.out.println("Test UserRepository.findById()");
        User dbUser = userRepo.save(getNewUser());
        assertNotNull(dbUser);
        assertEquals(dbUser, userRepo.findById(dbUser.getId()).orElseThrow());
        System.out.println("Found by id " + dbUser);
    }

    @Test
    void findUserByPhone() {
        System.out.println(DELIMITER);
        System.out.println("Test UserRepository.findUserByPhone()");
        User user = getNewUser();
        user.getPhones().add(new Phone(1L, user, "+1234567890"));
        User dbUser = userRepo.save(user);
        assertNotNull(dbUser);
        assertEquals(dbUser, userRepo.findUserByPhone("+1234567890").orElseThrow());
        System.out.println("Found by phone " + dbUser);
    }

    @Test
    void findUserByEmail() {
        System.out.println(DELIMITER);
        System.out.println("Test UserRepository.findUserByEmail()");
        User user = getNewUser();
        user.getEmails().add(new Email(1L, user, "payer@mail.com"));
        User dbUser = userRepo.save(user);
        assertNotNull(dbUser);
        assertEquals(dbUser, userRepo.findUserByEmail("payer@mail.com").orElseThrow());
        System.out.println("Found by email " + dbUser);
    }

    @Test
    void findAll() {
        System.out.println(DELIMITER);
        System.out.println("Test UserRepository.findAll()");
        User dbUser = userRepo.save(getNewUser());
        User dbTargetUser = userRepo.save(getNewTargetUser());
        assertNotNull(dbUser);
        assertNotNull(dbTargetUser);
        List<User> users = userRepo.findAll();
        assertEquals(2, users.size());
        System.out.println("Found all users" + users);
    }
}