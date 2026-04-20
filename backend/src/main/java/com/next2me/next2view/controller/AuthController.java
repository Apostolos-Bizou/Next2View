package com.next2me.next2view.controller;

import com.next2me.next2view.dto.AuthResponse;
import com.next2me.next2view.dto.LoginRequest;
import com.next2me.next2view.dto.RefreshRequest;
import com.next2me.next2view.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        String ip = httpRequest.getRemoteAddr();
        AuthResponse auth = authService.login(request, ip);
        if (auth.mfaRequired()) {
            return ResponseEntity.ok(auth);
        }
        // Cookie για backward compat
        String cookieHeader = String.format(
            "access_token=%s; Max-Age=900; Path=/; Secure; HttpOnly; SameSite=None",
            auth.accessToken()
        );
        response.addHeader("Set-Cookie", cookieHeader);
        // Επιστρέφουμε ΚΑΙ το token στο body για cross-origin clients
        return ResponseEntity.ok(auth);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshRequest request,
            HttpServletResponse response
    ) {
        AuthResponse auth = authService.refresh(request.refreshToken());
        String cookieHeader = String.format(
            "access_token=%s; Max-Age=900; Path=/; Secure; HttpOnly; SameSite=None",
            auth.accessToken()
        );
        response.addHeader("Set-Cookie", cookieHeader);
        return ResponseEntity.ok(auth);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @AuthenticationPrincipal String userId,
            HttpServletResponse response
    ) {
        if (userId != null && !userId.equals("anonymousUser")) {
            try {
                authService.logout(UUID.fromString(userId));
            } catch (Exception ignored) {}
        }
        String cookieHeader = "access_token=; Max-Age=0; Path=/; Secure; HttpOnly; SameSite=None";
        response.addHeader("Set-Cookie", cookieHeader);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserInfo> me(
            @AuthenticationPrincipal String userId
    ) {
        if (userId == null || userId.equals("anonymousUser")) {
            return ResponseEntity.status(401).build();
        }
        try {
            AuthResponse.UserInfo info = authService.getUserInfo(UUID.fromString(userId));
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal String userId,
            @RequestBody Map<String, String> body) {
        if (userId == null || userId.equals("anonymousUser"))
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        try {
            authService.changePassword(UUID.fromString(userId), body.get("currentPassword"), body.get("newPassword"));
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        try {
            authService.forgotPassword(email.trim().toLowerCase());
        } catch (Exception ignored) {}
        return ResponseEntity.ok(Map.of("message", "If this email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        if (token == null || newPassword == null || newPassword.length() < 8)
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid request"));
        try {
            authService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired token"));
        }
    }
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @AuthenticationPrincipal String userId,
            @RequestBody Map<String, String> body) {
        if (userId == null || userId.equals("anonymousUser"))
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        try {
            authService.registerUser(
                body.get("firstName"),
                body.get("lastName"),
                body.get("email"),
                body.get("password"),
                body.get("role")
            );
            return ResponseEntity.ok(Map.of("message", "User created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}