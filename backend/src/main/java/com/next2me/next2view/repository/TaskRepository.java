package com.next2me.next2view.repository;

import com.next2me.next2view.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for Task entities.
 *
 * Introduced in v5.4.0 (task file attachments). Tasks were previously managed
 * only nested through the project payload; this read-side repository lets the
 * TaskFileController load a task by id to resolve its parent project (for the
 * MFA gate and category scoping) without touching the existing project flow.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
}
