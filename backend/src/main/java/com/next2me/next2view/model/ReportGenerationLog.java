package com.next2me.next2view.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_generation_log")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReportGenerationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "template_id", nullable = false, length = 50)
    private String templateId;

    @Column(name = "template_name", nullable = false, length = 200)
    private String templateName;

    @Column(name = "generated_by", nullable = false)
    private java.util.UUID generatedBy;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "parameters", columnDefinition = "jsonb")
    private String parameters;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "SUCCESS";

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) generatedAt = LocalDateTime.now();
    }
}
