package com.next2me.next2view.controller;

import com.next2me.next2view.model.User;
import com.next2me.next2view.repository.UserRepository;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.*;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/mfa")
@RequiredArgsConstructor
@Slf4j
public class MfaController {

    private final UserRepository userRepo;

    // Step 1: Generate secret + QR code URI
    @PostMapping("/setup")
    public ResponseEntity<Map<String, String>> setup(
            @AuthenticationPrincipal String userId) {
        User user = userRepo.findById(UUID.fromString(userId))
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String secret = new DefaultSecretGenerator().generate();
        user.setMfaSecret(secret);
        user.setMfaEnabled(false); // not enabled until verified
        userRepo.save(user);

        String otpauthUrl = String.format(
            "otpauth://totp/Next2View:%s?secret=%s&issuer=Next2View",
            user.getEmail(), secret);

        return ResponseEntity.ok(Map.of(
            "secret", secret,
            "otpauthUrl", otpauthUrl,
            "email", user.getEmail()
        ));
    }

    // Step 2: Verify code and enable MFA
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(
            @AuthenticationPrincipal String userId,
            @RequestBody Map<String, String> body) {
        User user = userRepo.findById(UUID.fromString(userId))
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String code = body.get("code");
        if (code == null || user.getMfaSecret() == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid request"));

        boolean valid = verifyCode(user.getMfaSecret(), code);
        if (!valid)
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid code"));

        user.setMfaEnabled(true);
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("success", true, "mfaEnabled", true));
    }

    // Step 3: Disable MFA
    @PostMapping("/disable")
    public ResponseEntity<Map<String, Object>> disable(
            @AuthenticationPrincipal String userId,
            @RequestBody Map<String, String> body) {
        User user = userRepo.findById(UUID.fromString(userId))
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String code = body.get("code");
        if (!verifyCode(user.getMfaSecret(), code))
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid code"));

        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("success", true, "mfaEnabled", false));
    }

    // Status
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(
            @AuthenticationPrincipal String userId) {
        User user = userRepo.findById(UUID.fromString(userId))
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(Map.of("mfaEnabled", user.getMfaEnabled()));
    }

    private boolean verifyCode(String secret, String code) {
        try {
            CodeVerifier verifier = new DefaultCodeVerifier(
                new DefaultCodeGenerator(), new SystemTimeProvider());
            return verifier.isValidCode(secret, code);
        } catch (Exception e) {
            return false;
        }
    }
}