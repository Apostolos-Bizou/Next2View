package com.next2me.next2view.dto;

import java.util.UUID;

public record UserDto(
    UUID id,
    String fullName,
    String email,
    String role,
    String department,
    UUID companyId,
    String companyName,
    boolean active
) {}