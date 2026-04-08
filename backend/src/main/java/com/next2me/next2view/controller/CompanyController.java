package com.next2me.next2view.controller;

import com.next2me.next2view.dto.CompanyDto;
import com.next2me.next2view.dto.CompanyRequest;
import com.next2me.next2view.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<List<CompanyDto>> getAll() {
        return ResponseEntity.ok(companyService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDto> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('CEO')")
    public ResponseEntity<CompanyDto> create(
            @Valid @RequestBody CompanyRequest req,
            Principal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyService.create(req, principal.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CEO')")
    public ResponseEntity<CompanyDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody CompanyRequest req,
            Principal principal
    ) {
        return ResponseEntity.ok(companyService.update(id, req, principal.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CEO')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Principal principal
    ) {
        companyService.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}