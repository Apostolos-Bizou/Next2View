package com.next2me.next2view.repository;

import com.next2me.next2view.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findAllByActiveTrueOrderByUpdatedAtDesc();
    List<Project> findAllByCompanyIdAndActiveTrue(UUID companyId);
    List<Project> findAllByCategoryAndActiveTrue(Project.Category category);

    /**
     * Returns active projects matching the given company and category filter set.
     * Used by PermissionEvaluator-driven filtering for non-CEO users.
     * If optionalCompanyId is null, no company filter is applied (CEO case).
     * If optionalCategory is null, all categories from allowedCategories are used.
     */
    @Query("SELECT p FROM Project p WHERE p.active = true " +
           "AND (:companyId IS NULL OR p.company.id = :companyId) " +
           "AND (:useCategoryFilter = false OR p.category IN :categories) " +
           "ORDER BY p.updatedAt DESC")
    List<Project> findForScope(
            @org.springframework.data.repository.query.Param("companyId") UUID companyId,
            @org.springframework.data.repository.query.Param("useCategoryFilter") boolean useCategoryFilter,
            @org.springframework.data.repository.query.Param("categories") Collection<Project.Category> categories);

    @Query("SELECT p FROM Project p WHERE p.active = true AND p.status IN ('at_risk','stale')")
    List<Project> findAllAtRiskOrStale();
}
