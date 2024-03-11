package com.example.bankingservice.service;

import com.example.bankingservice.model.Email;
import com.example.bankingservice.repository.EmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final EmailRepository emailRepository;

    public EmailService(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    public Optional<Email> getEmailById(Long id) {
        return emailRepository.findById(id);
    }

    public List<Email> getAllEmails() {
        return emailRepository.findAll();
    }

    @Transactional
    public Email createEmail(Email email) {
        Email dbEmail = emailRepository.save(email);
        if (logger.isInfoEnabled()) {
            logger.info("Created email / Создан email {}", dbEmail);
        }
        return dbEmail;
    }

    @Transactional
    public List<Email> createEmails(List<Email> emails) {
        List<Email> dbEmails = emailRepository.saveAll(emails);
        if (logger.isInfoEnabled()) {
            logger.info("Created emails / Созданы email {}", dbEmails);
        }
        return dbEmails;
    }

    @Transactional
    public Optional<Email> updateEmail(Long id, Email updatedEmail) {
        Optional<Email> updated = emailRepository.findById(id).map(oldEmail -> emailRepository.save(updatedEmail));
        if (logger.isInfoEnabled()) {
            updated.ifPresent(n -> logger.info("Updated email / Обновлен email {}", updated.get()));
        }
        return updated;
    }

    @Transactional
    public void deleteEmail(Long id) {
        try {
            Optional<Email> email = getEmailById(id);
            if (email.isPresent() && email.get().getUser().getEmails().size() > 1) {
                emailRepository.deleteById(id);
                if (logger.isInfoEnabled()) {
                    logger.info("Deleted email with id / Удален email с id: {}", id);
                }
            } else {
                throw new EmptyResultDataAccessException("Can't delete the last email / Нельзя удалить последний email", 2);
            }
        } catch (EmptyResultDataAccessException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when deleting the email with id / Не удалось удалить email с id: {}", id);
            }
        }
    }
}
