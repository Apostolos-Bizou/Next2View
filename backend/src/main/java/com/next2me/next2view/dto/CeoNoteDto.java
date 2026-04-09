package com.next2me.next2view.dto;

import java.time.Instant;
import java.util.UUID;

public record CeoNoteDto(
    UUID id,
    String content,
    String createdBy,
    Instant createdAt
) {}