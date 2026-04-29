const fs = require('fs');

const dismissalEntity = `package com.next2me.next2view.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activity_dismissals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityDismissal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "activity_id", nullable = false)
    private UUID activityId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
`;

fs.writeFileSync('backend/src/main/java/com/next2me/next2view/model/ActivityDismissal.java', dismissalEntity, 'utf8');
console.log('DONE: ActivityDismissal.java rewritten without BOM');