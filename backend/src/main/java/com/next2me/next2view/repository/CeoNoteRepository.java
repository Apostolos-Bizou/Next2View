package com.next2me.next2view.repository;

import com.next2me.next2view.model.CeoNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CeoNoteRepository extends JpaRepository<CeoNote, UUID> {
    List<CeoNote> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}