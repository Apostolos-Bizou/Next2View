package com.next2me.next2view.repository;

import com.next2me.next2view.model.UserProjectScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserProjectScopeRepository extends JpaRepository<UserProjectScope, UUID> {

    List<UserProjectScope> findByUserId(UUID userId);

    @Query("SELECT s.project.id FROM UserProjectScope s WHERE s.user.id = :userId")
    Set<UUID> findProjectIdsByUserId(UUID userId);

    @Modifying
    @Transactional
    void deleteByUserId(UUID userId);

    boolean existsByUserIdAndProjectId(UUID userId, UUID projectId);
}
