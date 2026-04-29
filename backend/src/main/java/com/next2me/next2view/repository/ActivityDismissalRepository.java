package com.next2me.next2view.repository;

import com.next2me.next2view.model.ActivityDismissal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface ActivityDismissalRepository extends JpaRepository<ActivityDismissal, UUID> {

    @Query("SELECT d.activityId FROM ActivityDismissal d WHERE d.userId = :userId")
    Set<UUID> findDismissedActivityIdsByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM ActivityDismissal d WHERE d.userId = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

    boolean existsByUserIdAndActivityId(UUID userId, UUID activityId);
}
