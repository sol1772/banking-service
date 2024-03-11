package com.example.bankingservice.web.controller;

import com.example.bankingservice.model.Phone;
import com.example.bankingservice.service.PhoneService;
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
@RequestMapping("api/phones")
public class PhoneController {
    private static final Logger logger = LoggerFactory.getLogger(PhoneController.class);
    private final PhoneService phoneService;

    @Autowired
    public PhoneController(PhoneService phoneService) {
        this.phoneService = phoneService;
    }

    @GetMapping
    public ResponseEntity<List<Phone>> getAllPhones() {
        List<Phone> phones = phoneService.getAllPhones();
        return ResponseEntity.ok().body(phones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Phone> findPhone(@PathVariable("id") Long id) {
        Optional<Phone> phone = phoneService.getPhoneById(id);
        return ResponseEntity.of(phone);
    }

    @PostMapping("/add")
    public ResponseEntity<Phone> addPhone(@RequestBody Phone phone, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            returnErrorsToClient(bindingResult);
        }
        return getNewPhone(phone);
    }

    @PostMapping("/add-all")
    public ResponseEntity<List<Phone>> addPhones(@RequestBody List<Phone> phones, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            returnErrorsToClient(bindingResult);
        }
        List<Phone> created = phoneService.createPhones(phones);
        return ResponseEntity.ok().body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Phone> editPhone(@PathVariable Long id, @RequestBody Phone updatedPhone) {
        Optional<Phone> updated;
        try {
            updated = phoneService.updatePhone(id, updatedPhone);
        } catch (RuntimeException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when updating phone with id {}: {}", id, e.getMessage());
            }
            throw new AppRuntimeException(e.getMessage());
        }
        return updated.map(value -> ResponseEntity.ok().body(value)).orElseGet(() -> getNewPhone(updatedPhone));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Phone> deletePhone(@PathVariable("id") Long id) {
        phoneService.deletePhone(id);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<Phone> getNewPhone(@RequestBody Phone phone) {
        Phone created = phoneService.createPhone(phone);
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
