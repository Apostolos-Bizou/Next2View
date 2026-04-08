package com.next2me.next2view.dto;

import java.util.UUID;

public record UserRequest(
    String fullName,
    String email,
    String password,
    String role,
    String department,
    UUID companyId,
    boolean active
) {}