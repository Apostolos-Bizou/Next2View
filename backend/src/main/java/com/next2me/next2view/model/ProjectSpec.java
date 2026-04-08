package com.next2me.next2view.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_specs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_done", nullable = false)
    @Builder.Default
    private Boolean isDone = false;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
}
