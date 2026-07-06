package com.next2me.next2view.model;

import jakarta.persistence.*;
import lombok.*;
import com.next2me.next2view.util.HtmlSanitizer;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Module extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String color = "dev";

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    /**
     * Sanitize HTML content before persisting (defense-in-depth XSS protection).
     */
    @PrePersist
    @PreUpdate
    private void sanitizeHtmlContent() {
        if (this.description != null) {
            this.description = HtmlSanitizer.clean(this.description);
        }
    }
}
