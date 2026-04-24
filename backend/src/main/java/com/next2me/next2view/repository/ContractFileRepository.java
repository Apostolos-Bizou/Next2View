package com.next2me.next2view.repository;

import com.next2me.next2view.model.ContractFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractFileRepository extends JpaRepository<ContractFile, UUID> {

    /** All active (non-soft-deleted) files for a project, newest first. */
    @Query("SELECT f FROM ContractFile f WHERE f.project.id = :projectId " +
           "AND f.isActive = true AND f.deletedAt IS NULL " +
           "ORDER BY f.uploadedAt DESC")
    List<ContractFile> findActiveByProjectId(@Param("projectId") UUID projectId);

    /** Active file by id (for download/delete ops). */
    @Query("SELECT f FROM ContractFile f WHERE f.id = :id " +
           "AND f.isActive = true AND f.deletedAt IS NULL")
    Optional<ContractFile> findActiveById(@Param("id") UUID id);

    /** Duplicate detection by SHA-256 hash (within a project). */
    @Query("SELECT f FROM ContractFile f WHERE f.project.id = :projectId " +
           "AND f.sha256 = :sha256 AND f.deletedAt IS NULL")
    Optional<ContractFile> findByProjectIdAndSha256(
            @Param("projectId") UUID projectId,
            @Param("sha256") String sha256);

    /** Soft-deleted files eligible for hard purge after retention window. */
    @Query("SELECT f FROM ContractFile f WHERE f.deletedAt IS NOT NULL " +
           "AND f.deletedAt < :cutoff")
    List<ContractFile> findDeletedBefore(@Param("cutoff") java.time.Instant cutoff);

    /** Count of active files per project (dashboard). */
    @Query("SELECT COUNT(f) FROM ContractFile f WHERE f.project.id = :projectId " +
           "AND f.isActive = true AND f.deletedAt IS NULL")
    long countActiveByProjectId(@Param("projectId") UUID projectId);
}
