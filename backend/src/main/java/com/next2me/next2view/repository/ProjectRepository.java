package com.next2me.next2view.repository;

import com.next2me.next2view.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findAllByActiveTrueOrderByUpdatedAtDesc();
    List<Project> findAllByCompanyIdAndActiveTrue(UUID companyId);
    List<Project> findAllByCategoryAndActiveTrue(Project.Category category);

    @Query("SELECT p FROM Project p WHERE p.active = true AND p.status IN ('at_risk','stale')")
    List<Project> findAllAtRiskOrStale();
}
