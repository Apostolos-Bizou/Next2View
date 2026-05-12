package com.next2me.next2view.repository;

import com.next2me.next2view.model.UserCompanyScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserCompanyScopeRepository extends JpaRepository<UserCompanyScope, UUID> {

    List<UserCompanyScope> findByUserId(UUID userId);

    @Query("SELECT s.company.id FROM UserCompanyScope s WHERE s.user.id = :userId")
    Set<UUID> findCompanyIdsByUserId(UUID userId);

    @Modifying
    @Transactional
    void deleteByUserId(UUID userId);

    boolean existsByUserIdAndCompanyId(UUID userId, UUID companyId);
}
