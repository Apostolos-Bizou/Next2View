package com.next2me.next2view.dto;

import com.next2me.next2view.model.Project;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectDto(
    UUID id,
    String title,
    UUID companyId,
    String companyName,
    String companyCode,
    String companyColor,
    Project.Category category,
    Project.Status status,
    BigDecimal budget,
    BigDecimal paid,
    BigDecimal invoiced,
    LocalDate startDate,
    LocalDate deadline,
    String contractDesc,
    int completion,
    int tasksTotal,
    int tasksDone,
    long updatedAgo,
    List<ModuleDto> modules,
    List<SpecDto> specs
) {
    public record ModuleDto(
        UUID id, String name, String color, int completion,
        List<TaskDto> tasks
    ) {}
    public record TaskDto(
        UUID id, String name, String assignee, int progress,
        boolean isDone, boolean isBlocked, String blockNote,
        String comment, LocalDate deadline,
        Integer startWeek, Integer durationWeeks, Integer startDay, Integer durationDays, Boolean manualProgress, java.time.LocalDate startDate, java.time.LocalDate endDate
    ) {}
    public record SpecDto(UUID id, String description, boolean isDone, int sortOrder, java.time.LocalDate startDate, java.time.LocalDate endDate) {}
}