package com.next2me.next2view.dto;

import java.util.List;
import java.util.UUID;

/**
 * Per-user scope restrictions. Empty list = no restriction (default behavior).
 * Non-empty list = user is restricted to exactly the listed entities.
 *
 * Scopes are combined as AND with category permissions:
 *   final_visibility = category_allowed AND (companyScope_empty_or_contains) AND (projectScope_empty_or_contains)
 */
public record UserScopeDto(
    List<UUID> companyScope,
    List<UUID> projectScope
) {}
