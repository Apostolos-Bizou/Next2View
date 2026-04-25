package com.next2me.next2view.repository;

import com.next2me.next2view.model.ReportGenerationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportGenerationLogRepository extends JpaRepository<ReportGenerationLog, UUID> {

    @Query("SELECT r FROM ReportGenerationLog r ORDER BY r.generatedAt DESC")
    List<ReportGenerationLog> findAllOrderByDate();

    List<ReportGenerationLog> findByGeneratedByOrderByGeneratedAtDesc(Long userId);

    List<ReportGenerationLog> findByTemplateIdOrderByGeneratedAtDesc(String templateId);
}
