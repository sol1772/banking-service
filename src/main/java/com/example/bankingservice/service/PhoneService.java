package com.example.bankingservice.service;

import com.example.bankingservice.model.Phone;
import com.example.bankingservice.repository.PhoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PhoneService {
    private static final Logger logger = LoggerFactory.getLogger(PhoneService.class);
    private final PhoneRepository phoneRepository;

    public PhoneService(PhoneRepository phoneRepository) {
        this.phoneRepository = phoneRepository;
    }

    public Optional<Phone> getPhoneById(Long id) {
        return phoneRepository.findById(id);
    }

    public List<Phone> getAllPhones() {
        return phoneRepository.findAll();
    }

    @Transactional
    public Phone createPhone(Phone phone) {
        Phone dbPhone = phoneRepository.save(phone);
        if (logger.isInfoEnabled()) {
            logger.info("Created phone / Создан телефон {}", dbPhone);
        }
        return dbPhone;
    }

    @Transactional
    public List<Phone> createPhones(List<Phone> phones) {
        List<Phone> dbPhones = phoneRepository.saveAll(phones);
        if (logger.isInfoEnabled()) {
            logger.info("Created phones / Созданы телефоны {}", dbPhones);
        }
        return dbPhones;
    }

    @Transactional
    public Optional<Phone> updatePhone(Long id, Phone updatedPhone) {
        Optional<Phone> updated = phoneRepository.findById(id).map(oldPhone -> phoneRepository.save(updatedPhone));
        if (logger.isInfoEnabled()) {
            updated.ifPresent(n -> logger.info("Updated phone / Обновлен телефон {}", updated.get()));
        }
        return updated;
    }

    @Transactional
    public void deletePhone(Long id) {
        try {
            Optional<Phone> phone = getPhoneById(id);
            if (phone.isPresent() && phone.get().getUser().getPhones().size() > 1) {
                phoneRepository.deleteById(id);
                if (logger.isInfoEnabled()) {
                    logger.info("Deleted phone with id / Удален телефон с id: {}", id);
                }
            } else {
                throw new EmptyResultDataAccessException("Can't delete the last phone / Нельзя удалить последний телефон", 2);
            }
        } catch (EmptyResultDataAccessException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when deleting the phone with id / Не удалось удалить телефон с id: {}", id);
            }
        }
    }
}
