package com.next2me.next2view.repository;
import com.next2me.next2view.model.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserPermissionRepository extends JpaRepository<UserPermission, UUID> {
    Optional<UserPermission> findByUserId(UUID userId);
}