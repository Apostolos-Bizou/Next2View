package com.next2me.next2view.controller;

import com.next2me.next2view.dto.AuthResponse;
import com.next2me.next2view.dto.LoginRequest;
import com.next2me.next2view.dto.RefreshRequest;
import com.next2me.next2view.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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

        // Set access token as HttpOnly cookie
        setAccessTokenCookie(response, auth.accessToken());

        // Return response without token in body (it's in cookie)
        return ResponseEntity.ok(new AuthResponse(
                null, auth.tokenType(), auth.expiresIn(), auth.user(), false));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshRequest request,
            HttpServletResponse response
    ) {
        AuthResponse auth = authService.refresh(request.refreshToken());
        setAccessTokenCookie(response, auth.accessToken());
        return ResponseEntity.ok(new AuthResponse(
                null, auth.tokenType(), auth.expiresIn(), auth.user(), false));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @AuthenticationPrincipal String userId,
            HttpServletResponse response
    ) {
        authService.logout(UUID.fromString(userId));
        clearAccessTokenCookie(response);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(
            @AuthenticationPrincipal String userId
    ) {
        return ResponseEntity.ok(Map.of("userId", userId));
    }

    // ── Cookie helpers ──

    private void setAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(15 * 60);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void clearAccessTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("access_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
