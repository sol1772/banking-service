package com.example.bankingservice.web.controller;

import com.example.bankingservice.model.User;
import com.example.bankingservice.model.dto.PageRequestDto;
import com.example.bankingservice.service.UserService;
import com.example.bankingservice.service.validation.UserValidator;
import com.example.bankingservice.util.AppErrorResponse;
import com.example.bankingservice.util.AppRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import static com.example.bankingservice.util.ErrorsUtil.returnErrorsToClient;

@RestController
@RequestMapping("api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final Random RANDOMIZER = new Random();
    private final UserService userService;
    private final UserValidator userValidator;


    @Autowired
    public UserController(UserService userService, UserValidator userValidator) {
        this.userService = userService;
        this.userValidator = userValidator;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok().body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        Optional<User> user = userService.getUserById(id);
        return ResponseEntity.of(user);
    }

    @GetMapping("/random")
    public ResponseEntity<User> getRandomUser() {
        return getUser(nextLong(1, userService.getUserRepository().count() + 1));
    }

    @PostMapping("/add")
    public ResponseEntity<User> addUser(@RequestBody User user, BindingResult bindingResult) {
        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            returnErrorsToClient(bindingResult);
        }
        return getNewUser(user);
    }

    @PostMapping("/add-all")
    public ResponseEntity<List<User>> addUsers(@RequestBody List<User> users, BindingResult bindingResult) {
        for (User user : users) {
            userValidator.validate(user, bindingResult);
        }
        if (bindingResult.hasErrors()) {
            returnErrorsToClient(bindingResult);
        }
        List<User> created = userService.createUsers(users);
        return ResponseEntity.ok().body(created);
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> findAll() {
        PageRequestDto dto = new PageRequestDto();
        dto.setSort(Sort.Direction.ASC);
        dto.setSortByColumn("username");
        List<User> users = userService.getPageAllUsers(dto).getContent();
        return ResponseEntity.ok().body(users);
    }

    @GetMapping("/all-desc")
    public ResponseEntity<List<User>> findAllDesc() {
        PageRequestDto dto = new PageRequestDto();
        dto.setSort(Sort.Direction.DESC);
        dto.setSortByColumn("username");
        List<User> users = userService.getPageAllUsers(dto).getContent();
        return ResponseEntity.ok().body(users);
    }

    @GetMapping("/find-by-name")
    public ResponseEntity<List<User>> findByUsername(@RequestParam(name = "username", required = false) String username) {
        List<User> users;
        PageRequestDto dto = new PageRequestDto();
        dto.setSort(Sort.Direction.ASC);
        dto.setSortByColumn("username");
        if (Objects.isNull(username)) {
            users = userService.getPageAllUsers(dto).getContent();
        } else {
            users = userService.getPageUsersByUsername(username, dto).getContent();
        }
        return ResponseEntity.ok().body(users);
    }

    @GetMapping("/find-by-dob")
    public ResponseEntity<List<User>> findByDateOfBirth(@RequestParam(name = "dateOfBirth") LocalDate dateOfBirth) {
        List<User> users;
        PageRequestDto dto = new PageRequestDto();
        dto.setSort(Sort.Direction.ASC);
        dto.setSortByColumn("username");
        if (Objects.isNull(dateOfBirth)) {
            users = userService.getPageAllUsers(dto).getContent();
        } else {
            users = userService.getPageUsersByDateOfBirth(dateOfBirth, dto).getContent();
        }
        return ResponseEntity.ok().body(users);
    }

    @GetMapping("/find-by-phone")
    public ResponseEntity<User> getUserByPhone(@RequestParam(name = "phone") String number) {
        Optional<User> user = userService.getUserByPhone(number);
        return ResponseEntity.of(user);
    }

    @GetMapping("/find-by-email")
    public ResponseEntity<User> getUserByEmail(@RequestParam(name = "email") String email) {
        Optional<User> user = userService.getUserByEmail(email);
        return ResponseEntity.of(user);
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<User> changePassword(@PathVariable Long id, String oldPassword, String newPassword) {
        Optional<User> user = userService.getUserById(id);
        User updated = null;
        if (user.isPresent()) {
            try {
                updated = userService.changePassword(oldPassword, newPassword, user.get());
            } catch (RuntimeException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error when changing password for user with id {}: {}", id, e.getMessage());
                }
                throw new AppRuntimeException(e.getMessage());
            }
        }
        return ResponseEntity.ok().body(updated);
    }

//    updating prohibited by terms of the task
//    @PutMapping("/{id}")
//    public ResponseEntity<User> editUser(@PathVariable Long id, @RequestBody User updatedUser) {
//        Optional<User> updated;
//        try {
//            updated = userService.updateUser(id, updatedUser);
//        } catch (RuntimeException e) {
//            if (logger.isErrorEnabled()) {
//                logger.error("Error when updating user with id {}: {}", id, e.getMessage());
//            }
//            throw new AppRuntimeException(e.getMessage());
//        }
//        return updated.map(value -> ResponseEntity.ok().body(value)).orElseGet(() -> getNewUser(updatedUser));
//    }

    public ResponseEntity<User> getNewUser(@RequestBody User user) {
        User created = userService.createUser(user);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    private long nextLong(long lowerRange, long upperRange) {
        return (long) (RANDOMIZER.nextDouble() * (upperRange - lowerRange)) + lowerRange;
    }

    @ExceptionHandler
    private ResponseEntity<AppErrorResponse> handleException(AppRuntimeException e) {
        AppErrorResponse response = new AppErrorResponse(e.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
