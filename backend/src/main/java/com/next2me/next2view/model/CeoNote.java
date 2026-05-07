package com.next2me.next2view.model;

import jakarta.persistence.*;
import lombok.*;
import com.next2me.next2view.util.HtmlSanitizer;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ceo_notes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CeoNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    /**
     * Sanitize HTML content before persisting (defense-in-depth XSS protection).
     */
    @PrePersist
    @PreUpdate
    private void sanitizeHtmlContent() {
        if (this.content != null) {
            this.content = HtmlSanitizer.clean(this.content);
        }
    }
}
