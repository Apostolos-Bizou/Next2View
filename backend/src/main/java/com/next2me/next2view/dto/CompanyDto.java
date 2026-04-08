package com.next2me.next2view.dto;

import java.util.UUID;

public record CompanyDto(
    UUID id,
    String name,
    String code,
    String color,
    String description,
    int projectCount,
    int avgCompletion
) {}