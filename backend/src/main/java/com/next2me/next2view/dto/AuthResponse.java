package com.next2me.next2view.dto;

public record AuthResponse(
    String accessToken,
    String tokenType,
    int expiresIn,
    UserInfo user,
    boolean mfaRequired
) {
    public record UserInfo(
        String id,
        String fullName,
        String email,
        String role,
        String department
    ) {}
}
