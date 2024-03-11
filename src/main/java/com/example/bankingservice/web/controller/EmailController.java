package com.example.bankingservice.web.controller;

import com.example.bankingservice.model.Email;
import com.example.bankingservice.service.EmailService;
import com.example.bankingservice.util.AppErrorResponse;
import com.example.bankingservice.util.AppRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static com.example.bankingservice.util.ErrorsUtil.returnErrorsToClient;

@RestController
@RequestMapping("api/emails")
public class EmailController {
    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);
    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping
    public ResponseEntity<List<Email>> getAllEmails() {
        List<Email> phones = emailService.getAllEmails();
        return ResponseEntity.ok().body(phones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Email> findEmail(@PathVariable("id") Long id) {
        Optional<Email> email = emailService.getEmailById(id);
        return ResponseEntity.of(email);
    }

    @PostMapping("/add")
    public ResponseEntity<Email> addEmail(@RequestBody Email email, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            returnErrorsToClient(bindingResult);
        }
        return getNewEmail(email);
    }

    @PostMapping("/add-all")
    public ResponseEntity<List<Email>> addEmails(@RequestBody List<Email> emails, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            returnErrorsToClient(bindingResult);
        }
        List<Email> created = emailService.createEmails(emails);
        return ResponseEntity.ok().body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Email> editEmail(@PathVariable Long id, @RequestBody Email updatedEmail) {
        Optional<Email> updated;
        try {
            updated = emailService.updateEmail(id, updatedEmail);
        } catch (RuntimeException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when updating email with id {}: {}", id, e.getMessage());
            }
            throw new AppRuntimeException(e.getMessage());
        }
        return updated.map(value -> ResponseEntity.ok().body(value)).orElseGet(() -> getNewEmail(updatedEmail));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Email> deleteEmail(@PathVariable("id") Long id) {
        emailService.deleteEmail(id);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<Email> getNewEmail(@RequestBody Email email) {
        Email created = emailService.createEmail(email);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @ExceptionHandler
    private ResponseEntity<AppErrorResponse> handleException(AppRuntimeException e) {
        AppErrorResponse response = new AppErrorResponse(e.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
