package com.example.bankingservice.service;

import com.example.bankingservice.model.BankAccount;
import com.example.bankingservice.model.User;
import com.example.bankingservice.model.dto.PageRequestDto;
import com.example.bankingservice.repository.BankAccountRepository;
import com.example.bankingservice.repository.UserRepository;
import com.example.bankingservice.util.AppRuntimeException;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Getter
@Transactional(readOnly = true)
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;

    public UserService(UserRepository userRepository, BankAccountRepository bankAccountRepository) {
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User getUserByLogin(String login) {
        return userRepository.findUserByLogin(login);
    }

    public Optional<User> getUserByPhone(String number) {
        return userRepository.findUserByPhone(number);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<BankAccount> getAllBankAccounts() {
        return bankAccountRepository.findAll();
    }

    @Transactional
    public User createUser(User user) {
        User dbUser = userRepository.save(user);
        if (logger.isInfoEnabled()) {
            logger.info("Created user / Создан пользователь {}", dbUser);
        }
        return dbUser;
    }

    @Transactional
    public List<User> createUsers(List<User> users) {
        List<User> dbUsers = userRepository.saveAll(users);
        if (logger.isInfoEnabled()) {
            logger.info("Created users / Созданы пользователи {}", dbUsers);
        }
        return dbUsers;
    }

    @Transactional
    public Optional<User> updateUser(Long id, User updatedUser) {
        Optional<User> updated = userRepository.findById(id).map(oldUser -> userRepository.save(updatedUser));
        if (logger.isInfoEnabled()) {
            updated.ifPresent(n -> logger.info("Updated user / Обновлен пользователь {}", updated.get()));
        }
        return updated;
    }

    public Page<User> getPageAllUsers(PageRequestDto dto) {
        Pageable pageable = new PageRequestDto().getPageable(dto);
        return userRepository.findAll(pageable);
    }

    public Page<User> getPageUsersByDateOfBirth(LocalDate dateOfBirth, PageRequestDto dto) {
        Pageable pageable = new PageRequestDto().getPageable(dto);
        return userRepository.findByDateOfBirthAfter(dateOfBirth, pageable);
    }

    public Page<User> getPageUsersByUsername(String substring, PageRequestDto dto) {
        String searchString = substring + "%";
        Pageable pageable = new PageRequestDto().getPageable(dto);
        return userRepository.findByUsernameLike(searchString, pageable);
    }


    public boolean passwordIsValid(String password, User user) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return StringUtils.isEmpty(user.getPasswordHash()) ||
                bCryptPasswordEncoder.matches(password, user.getPasswordHash());
    }

    @Transactional
    public User changePassword(String oldPassword, String newPassword, User user) {
        User dbUser;
        if (passwordIsValid(oldPassword, user)) {
            String newPasswordHash = user.hashPassword(newPassword);
            user.setPasswordHash(newPasswordHash);
            dbUser = userRepository.save(user);
            if (logger.isInfoEnabled()) {
                logger.info("Password changed successfully for / Пароль успешно изменен для {}", dbUser);
            }
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("Password change error for / Ошибка изменения пароля для {} " +
                        "(old password is incorrect / старый пароль неверный)", user);
            }
            throw new AppRuntimeException("Old password is incorrect / Старый пароль неверный!");
        }
        return dbUser;
    }
}
