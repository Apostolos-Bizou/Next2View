package com.next2me.next2view.dto;

import com.next2me.next2view.model.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectRequest(
    @NotBlank @Size(max = 255) String title,
    @NotNull UUID companyId,
    @NotNull Project.Category category,
    BigDecimal budget,
    BigDecimal paid,
    BigDecimal invoiced,
    LocalDate startDate,
    LocalDate deadline,
    String contractDesc,
    List<SpecRequest> specs,
    List<ModuleRequest> modules
) {
    public record SpecRequest(String description, boolean isDone, int sortOrder, LocalDate startDate, LocalDate endDate) {}
    public record ModuleRequest(
        String name, String color, int sortOrder,
        List<TaskRequest> tasks
    ) {}
    public record TaskRequest(
        String name, String assignee, int progress,
        boolean isDone, boolean isBlocked,
        String blockNote, String comment,
        LocalDate startDate,
    LocalDate deadline, Integer startWeek, Integer durationWeeks, Integer startDay, Integer durationDays, Boolean manualProgress, LocalDate endDate,
        int sortOrder
    ) {}
}