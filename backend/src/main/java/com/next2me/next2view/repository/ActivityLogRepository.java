package com.next2me.next2view.repository;

import com.next2me.next2view.model.ActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    /** All recent — for CEO */
    @Query("SELECT a FROM ActivityLog a ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecent(Pageable pageable);

    /** All recent since timestamp — for CEO */
    @Query("SELECT a FROM ActivityLog a WHERE a.createdAt > :since ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecentSince(@Param("since") Instant since, Pageable pageable);

    /** Filtered by categories — for non-CEO (category NULL = visible to all) */
    @Query("SELECT a FROM ActivityLog a WHERE a.category IN :categories OR a.category IS NULL ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecentByCategories(@Param("categories") List<String> categories, Pageable pageable);

    /** Filtered by categories + since timestamp */
    @Query("SELECT a FROM ActivityLog a WHERE a.createdAt > :since AND (a.category IN :categories OR a.category IS NULL) ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecentByCategoriesSince(
            @Param("categories") List<String> categories,
            @Param("since") Instant since,
            Pageable pageable);

    /** By actor */
    List<ActivityLog> findByActorIdOrderByCreatedAtDesc(UUID actorId, Pageable pageable);

    /** By entity (e.g. project history) */
    List<ActivityLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, UUID entityId);
}
