package com.next2me.next2view.controller;

import com.next2me.next2view.dto.UserDto;
import com.next2me.next2view.dto.UserRequest;
import com.next2me.next2view.model.User;
import com.next2me.next2view.model.Company;
import com.next2me.next2view.repository.CompanyRepository;
import com.next2me.next2view.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CEO')")
public class AdminController {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@RequestBody UserRequest req) {
        User user = new User();
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password() != null ? req.password() : "Next2View@2026!"));
        user.setRole(User.Role.valueOf(req.role()));
        if (req.department() != null && !req.department().isBlank()) {
            user.setDepartment(User.Department.valueOf(req.department()));
        }
        if (req.companyId() != null) {
            companyRepository.findById(req.companyId()).ifPresent(user::setCompany);
        }
        user.setActive(true);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID id, @RequestBody UserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setRole(User.Role.valueOf(req.role()));
        if (req.department() != null && !req.department().isBlank()) {
            user.setDepartment(User.Department.valueOf(req.department()));
        } else {
            user.setDepartment(null);
        }
        if (req.companyId() != null) {
            companyRepository.findById(req.companyId()).ifPresent(user::setCompany);
        } else {
            user.setCompany(null);
        }
        user.setActive(req.active());
        if (req.password() != null && !req.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.password()));
        }
        userRepository.save(user);
        return ResponseEntity.ok(toDto(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setActive(false);
            userRepository.save(u);
        });
        return ResponseEntity.noContent().build();
    }

    private UserDto toDto(User u) {
        return new UserDto(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getRole().name(),
                u.getDepartment() != null ? u.getDepartment().name() : null,
                u.getCompany() != null ? u.getCompany().getId() : null,
                u.getCompany() != null ? u.getCompany().getName() : null,
                u.isActive() != null ? u.isActive() : true
        );
    }
}