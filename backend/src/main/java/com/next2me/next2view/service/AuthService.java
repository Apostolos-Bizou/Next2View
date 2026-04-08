package com.next2me.next2view.service;

import com.next2me.next2view.dto.AuthResponse;
import com.next2me.next2view.dto.LoginRequest;
import com.next2me.next2view.model.RefreshToken;
import com.next2me.next2view.model.User;
import com.next2me.next2view.repository.AuditLogRepository;
import com.next2me.next2view.repository.RefreshTokenRepository;
import com.next2me.next2view.repository.UserRepository;
import com.next2me.next2view.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditLogRepository auditLogRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.jwt.refresh-token-expiry-days:7}")
    private int refreshTokenExpiryDays;

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByEmailAndActiveTrue(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        // Check lockout
        if (user.isLocked()) {
            throw new LockedException("Account locked. Try again later.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedAttempt(user);
            throw new BadCredentialsException("Invalid credentials");
        }

        // MFA check for CEO
        if (user.getMfaEnabled() && user.getRole() == User.Role.CEO) {
            if (request.mfaCode() == null || request.mfaCode().isBlank()) {
                return new AuthResponse(null, null, 0,
                        buildUserInfo(user), true);
            }
            // MFA validation would go here via TotpService
        }

        // Reset failed attempts on success
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        String rawRefresh = UUID.randomUUID().toString();
        String refreshHash = hashToken(rawRefresh);

        // Save refresh token
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(refreshHash)
                .expiresAt(Instant.now().plusSeconds(refreshTokenExpiryDays * 86400L))
                .build());

        log.info("Login success: {} from {}", user.getEmail(), ipAddress);

        return new AuthResponse(
                accessToken,
                "Bearer",
                15 * 60,
                buildUserInfo(user),
                false
        );
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        String hash = hashToken(rawRefreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (!token.isValid()) {
            throw new BadCredentialsException("Refresh token expired or revoked");
        }

        User user = token.getUser();
        String newAccess = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());

        return new AuthResponse(newAccess, "Bearer", 15 * 60, buildUserInfo(user), false);
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Logout: all refresh tokens revoked for user {}", userId);
    }

    private void handleFailedAttempt(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);
        if (attempts >= MAX_ATTEMPTS) {
            user.setLockedUntil(Instant.now().plusSeconds(LOCKOUT_MINUTES * 60L));
            log.warn("Account locked: {} after {} failed attempts", user.getEmail(), attempts);
        }
        userRepository.save(user);
    }

    private String hashToken(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Token hashing failed", e);
        }
    }

    private AuthResponse.UserInfo buildUserInfo(User user) {
        return new AuthResponse.UserInfo(
                user.getId().toString(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.getDepartment() != null ? user.getDepartment().name() : null
        );
    }
}
