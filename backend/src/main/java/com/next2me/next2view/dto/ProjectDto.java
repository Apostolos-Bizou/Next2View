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
    String description,
    int completion,
    int tasksTotal,
    int tasksDone,
    long updatedAgo,
    Integer overdueTaskCount,
    List<ModuleDto> modules,
    List<SpecDto> specs,
    Boolean workPlanEnabled
) {
    public record ModuleDto(
        UUID id, String name, String color, int completion,
        String description,
        List<TaskDto> tasks
    ) {}
    public record TaskDto(
        UUID id, String name, String assignee, int progress,
        boolean isDone, boolean isBlocked, String blockNote,
        String comment, String description, LocalDate deadline,
        Integer startWeek, Integer durationWeeks, Integer startDay, Integer durationDays, Boolean manualProgress, java.time.LocalDate startDate, java.time.LocalDate endDate,
        java.time.LocalTime startTime, java.time.LocalTime endTime,
        String environment, java.math.BigDecimal workDays, Boolean isGate
    ) {}
    public record SpecDto(UUID id, String description, boolean isDone, int sortOrder, java.time.LocalDate startDate, java.time.LocalDate endDate) {}
}
