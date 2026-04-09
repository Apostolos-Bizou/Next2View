package com.next2me.next2view.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Task extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 150)
    private String assignee;

    @Column(nullable = false)
    @Builder.Default
    private Integer progress = 0;

    @Column(name = "is_done", nullable = false)
    @Builder.Default
    private Boolean isDone = false;

    @Column(name = "is_blocked", nullable = false)
    @Builder.Default
    private Boolean isBlocked = false;

    @Column(name = "block_note", columnDefinition = "TEXT")
    private String blockNote;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private LocalDate deadline;

    @Column(name = "start_week")
    private Integer startWeek;

    @Column(name = "duration_weeks")
    @Builder.Default
    private Integer durationWeeks = 1;

    @Column(name = "start_day")
    private Integer startDay;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "manual_progress")
    @Builder.Default
    private Boolean manualProgress = false;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
