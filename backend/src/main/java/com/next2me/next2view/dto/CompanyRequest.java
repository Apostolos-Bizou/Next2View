package com.next2me.next2view.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
    @NotBlank @Size(max = 150) String name,
    @NotBlank @Size(max = 5)   String code,
    @NotBlank                  String color,
    String description
) {}