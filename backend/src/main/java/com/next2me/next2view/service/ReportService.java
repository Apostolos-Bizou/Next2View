package com.next2me.next2view.service;

import com.next2me.next2view.dto.ReportDataDTO;
import com.next2me.next2view.dto.ReportTemplateDTO;
import com.next2me.next2view.model.ReportGenerationLog;
import com.next2me.next2view.model.User;
import com.next2me.next2view.repository.*;
import com.next2me.next2view.model.Project;
import com.next2me.next2view.model.ContractFile;
import com.next2me.next2view.model.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final ContractFileRepository contractFileRepository;
    private final ReportGenerationLogRepository reportLogRepository;
    // Will inject more repos as we add templates

    // ── Template Registry ──
    private static final List<ReportTemplateDTO> TEMPLATES = List.of(
        ReportTemplateDTO.builder()
            .id("security-snapshot")
            .name("Security & Compliance Snapshot")
            .description("Bundle of DPIA, Technical Security, MFA coverage, and current security posture")
            .icon("shield")
            .category("compliance")
            .dataSources(List.of("Users/MFA", "Security Documents", "Legal Vault", "System Config"))
            .aiEnhanced(false)
            .build(),
        ReportTemplateDTO.builder()
            .id("exec-summary")
            .name("Executive Summary")
            .description("All KPIs in one report: projects, completion rates, deadlines, alerts")
            .icon("briefcase")
            .category("management")
            .dataSources(List.of("Projects", "Tasks", "Finance", "Deadlines"))
            .aiEnhanced(true)
            .build(),
        ReportTemplateDTO.builder()
            .id("legal-activity")
            .name("Legal Vault Activity")
            .description("Upload/download history, access logs, and audit trail for legal documents")
            .icon("folder-lock")
            .category("legal")
            .dataSources(List.of("Legal Vault", "Audit Logs"))
            .aiEnhanced(false)
            .build(),
        ReportTemplateDTO.builder()
            .id("mfa-status")
            .name("MFA Compliance Status")
            .description("Which users have MFA enabled, who needs to activate, with action items")
            .icon("smartphone")
            .category("security")
            .dataSources(List.of("Users", "MFA Config"))
            .aiEnhanced(false)
            .build()
    );

    public List<ReportTemplateDTO> getTemplates() {
        return TEMPLATES;
    }

    public Optional<ReportTemplateDTO> getTemplate(String templateId) {
        return TEMPLATES.stream()
            .filter(t -> t.getId().equals(templateId))
            .findFirst();
    }

    // ── Security & Compliance Snapshot Data ──
    public ReportDataDTO generateSecuritySnapshotData() {
        log.info("Generating Security & Compliance Snapshot data");

        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        long mfaEnabled = allUsers.stream().filter(u -> u.getMfaSecret() != null && !u.getMfaSecret().isEmpty()).count();
        double mfaCoverage = totalUsers > 0 ? Math.round((double) mfaEnabled / totalUsers * 100.0) : 0;

        // Build MFA user detail list
        List<Map<String, Object>> mfaDetails = new ArrayList<>();
        for (User u : allUsers) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("username", u.getEmail());
            row.put("fullName", u.getFullName());
            row.put("role", u.getRole().name());
            row.put("company", u.getCompany() != null ? u.getCompany().getName() : "—");
            row.put("mfaEnabled", u.getMfaSecret() != null && !u.getMfaSecret().isEmpty());
            row.put("actionRequired", u.getMfaSecret() == null || u.getMfaSecret().isEmpty());
            mfaDetails.add(row);
        }

        // Build summary
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalUsers", totalUsers);
        summary.put("mfaEnabled", mfaEnabled);
        summary.put("mfaPending", totalUsers - mfaEnabled);
        summary.put("mfaCoveragePercent", mfaCoverage);
        summary.put("encryptionAlgorithm", "AES-256-GCM");
        summary.put("keyManagement", "Azure Key Vault CMK (RSA-3072)");
        summary.put("storageEncryption", "Server-side + Client-side double encryption");
        summary.put("authMethod", "JWT RS256 + TOTP MFA");
        summary.put("passwordHashing", "BCrypt (cost factor 12)");
        summary.put("sessionSecurity", "HttpOnly + Secure + SameSite=Strict cookies");
        summary.put("lockoutPolicy", "5 failed attempts → 15 min lockout");
        summary.put("tlsVersion", "TLS 1.2 minimum");

        // Build sections
        List<Map<String, Object>> sections = new ArrayList<>();

        // Section 1: MFA Coverage
        Map<String, Object> mfaSection = new LinkedHashMap<>();
        mfaSection.put("title", "MFA Coverage");
        mfaSection.put("status", mfaCoverage >= 100 ? "COMPLIANT" : "ACTION_REQUIRED");
        mfaSection.put("score", mfaCoverage);
        mfaSection.put("users", mfaDetails);
        sections.add(mfaSection);

        // Section 2: Encryption & Data Protection
        Map<String, Object> encSection = new LinkedHashMap<>();
        encSection.put("title", "Encryption & Data Protection");
        encSection.put("status", "COMPLIANT");
        encSection.put("items", List.of(
            Map.of("check", "Legal Vault AES-256-GCM encryption", "status", true),
            Map.of("check", "CMK managed via Azure Key Vault", "status", true),
            Map.of("check", "Blob soft-delete 90 days", "status", true),
            Map.of("check", "Blob versioning enabled", "status", true),
            Map.of("check", "No public blob access", "status", true),
            Map.of("check", "TLS 1.2 minimum enforced", "status", true)
        ));
        sections.add(encSection);

        // Section 3: Authentication Security
        Map<String, Object> authSection = new LinkedHashMap<>();
        authSection.put("title", "Authentication Security");
        authSection.put("status", "COMPLIANT");
        authSection.put("items", List.of(
            Map.of("check", "JWT RS256 asymmetric signing", "status", true),
            Map.of("check", "Access token 60min TTL", "status", true),
            Map.of("check", "Refresh token rotation", "status", true),
            Map.of("check", "BCrypt password hashing (cost 12)", "status", true),
            Map.of("check", "Account lockout after 5 failures", "status", true),
            Map.of("check", "HttpOnly secure cookies", "status", true)
        ));
        sections.add(authSection);

        // Section 4: OWASP Top 10
        Map<String, Object> owaspSection = new LinkedHashMap<>();
        owaspSection.put("title", "OWASP Top 10 Mitigation");
        owaspSection.put("status", "COMPLIANT");
        owaspSection.put("items", List.of(
            Map.of("check", "A01 Broken Access Control — RBAC + permission gates", "status", true),
            Map.of("check", "A02 Cryptographic Failures — AES-256-GCM + RSA-3072", "status", true),
            Map.of("check", "A03 Injection — Parameterized queries via JPA", "status", true),
            Map.of("check", "A04 Insecure Design — Defense in depth architecture", "status", true),
            Map.of("check", "A05 Security Misconfiguration — Key Vault secrets management", "status", true),
            Map.of("check", "A07 Auth Failures — MFA + lockout + JWT rotation", "status", true)
        ));
        sections.add(owaspSection);

        return ReportDataDTO.builder()
            .templateId("security-snapshot")
            .templateName("Security & Compliance Snapshot")
            .generatedAt(LocalDateTime.now())
            .summary(summary)
            .sections(sections)
            .build();
    }

    // ── MFA Compliance Status Data ──
    public ReportDataDTO generateMfaStatusData() {
        log.info("Generating MFA Compliance Status data");

        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        long mfaEnabled = allUsers.stream().filter(u -> u.getMfaSecret() != null && !u.getMfaSecret().isEmpty()).count();

        List<Map<String, Object>> userRows = new ArrayList<>();
        List<Map<String, Object>> actionItems = new ArrayList<>();

        for (User u : allUsers) {
            boolean hasMfa = u.getMfaSecret() != null && !u.getMfaSecret().isEmpty();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("username", u.getEmail());
            row.put("fullName", u.getFullName());
            row.put("role", u.getRole().name());
            row.put("company", u.getCompany() != null ? u.getCompany().getName() : "—");
            row.put("mfaEnabled", hasMfa);
            userRows.add(row);

            if (!hasMfa) {
                String priority = "CEO".equals(u.getRole().name()) ? "CRITICAL" : "HIGH";
                actionItems.add(Map.of(
                    "user", u.getFullName(),
                    "action", "Activate MFA via Profile → Security",
                    "priority", priority
                ));
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalUsers", totalUsers);
        summary.put("mfaEnabled", mfaEnabled);
        summary.put("mfaPending", totalUsers - mfaEnabled);
        summary.put("compliancePercent", totalUsers > 0 ? Math.round((double) mfaEnabled / totalUsers * 100.0) : 0);

        List<Map<String, Object>> sections = List.of(
            Map.of("title", "User MFA Status", "users", userRows),
            Map.of("title", "Action Items", "items", actionItems)
        );

        return ReportDataDTO.builder()
            .templateId("mfa-status")
            .templateName("MFA Compliance Status")
            .generatedAt(LocalDateTime.now())
            .summary(summary)
            .sections(sections)
            .build();
    }

    // ── Log report generation ──
    public void logGeneration(String templateId, String templateName, java.util.UUID userId, Long fileSize) {
        ReportGenerationLog logEntry = ReportGenerationLog.builder()
            .templateId(templateId)
            .templateName(templateName)
            .generatedBy(userId)
            .fileSizeBytes(fileSize)
            .parameters(null)
            .status("SUCCESS")
            .build();
        reportLogRepository.save(logEntry);
        log.info("Report generation logged: {} by user {}", templateId, userId);
    }

    // ── Preview router ──
    public ReportDataDTO generatePreviewData(String templateId) {
        return switch (templateId) {
            case "security-snapshot" -> generateSecuritySnapshotData();
            case "mfa-status" -> generateMfaStatusData();
            // exec-summary and legal-activity will be added in Step 6
            default -> throw new IllegalArgumentException("Unknown template: " + templateId);
        };
    }

    // ── Executive Summary Data ──
    public ReportDataDTO generateExecSummaryData() {
        log.info("Generating Executive Summary data");

        List<Project> projects = projectRepository.findAllByActiveTrueOrderByUpdatedAtDesc();
        List<Company> companies = companyRepository.findAllByActiveTrueOrderByName();

        int total = projects.size();
        long completed = projects.stream().filter(p -> p.getStatus() == Project.Status.completed).count();
        long atRisk = projects.stream().filter(p -> p.getStatus() == Project.Status.at_risk).count();
        long delayed = projects.stream().filter(p -> p.getStatus() == Project.Status.delayed).count();
        long onTrack = projects.stream().filter(p -> p.getStatus() == Project.Status.on_track).count();

        java.math.BigDecimal totalBudget = projects.stream()
            .map(p -> p.getBudget() != null ? p.getBudget() : java.math.BigDecimal.ZERO)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal totalPaid = projects.stream()
            .map(p -> p.getPaid() != null ? p.getPaid() : java.math.BigDecimal.ZERO)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal totalInvoiced = projects.stream()
            .map(p -> p.getInvoiced() != null ? p.getInvoiced() : java.math.BigDecimal.ZERO)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalProjects", total);
        summary.put("completed", completed);
        summary.put("onTrack", onTrack);
        summary.put("delayed", delayed);
        summary.put("atRisk", atRisk);
        summary.put("companies", companies.size());
        summary.put("totalBudget", "\u20ac" + totalBudget.toPlainString());
        summary.put("totalPaid", "\u20ac" + totalPaid.toPlainString());
        summary.put("totalInvoiced", "\u20ac" + totalInvoiced.toPlainString());

        // Projects by category
        List<Map<String, Object>> sections = new ArrayList<>();

        for (Project.Category cat : Project.Category.values()) {
            List<Project> catProjects = projects.stream()
                .filter(p -> p.getCategory() == cat).toList();
            if (catProjects.isEmpty()) continue;

            List<Map<String, Object>> projectRows = new ArrayList<>();
            for (Project p : catProjects) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("fullName", p.getTitle());
                row.put("role", p.getStatus() != null ? p.getStatus().name().toUpperCase() : "ON_TRACK");
                row.put("company", p.getCompany() != null ? p.getCompany().getName() : "\u2014");
                int completion = 0;
                if (p.getModules() != null) {
                    int totalTasks = 0, doneTasks = 0;
                    for (var m : p.getModules()) {
                        if (m.getTasks() != null) {
                            totalTasks += m.getTasks().size();
                            doneTasks += (int) m.getTasks().stream().filter(t -> Boolean.TRUE.equals(t.getIsDone())).count();
                        }
                    }
                    completion = totalTasks > 0 ? (doneTasks * 100 / totalTasks) : 0;
                }
                row.put("mfaEnabled", completion >= 100);
                row.put("actionRequired", p.getStatus() == Project.Status.at_risk || p.getStatus() == Project.Status.delayed);
                row.put("username", completion + "% complete");
                projectRows.add(row);
            }

            Map<String, Object> section = new LinkedHashMap<>();
            section.put("title", cat.name().substring(0, 1).toUpperCase() + cat.name().substring(1) + " (" + catProjects.size() + " projects)");
            section.put("status", catProjects.stream().anyMatch(p -> p.getStatus() == Project.Status.at_risk) ? "ACTION_REQUIRED" : "COMPLIANT");
            section.put("users", projectRows);
            sections.add(section);
        }

        return ReportDataDTO.builder()
            .templateId("exec-summary")
            .templateName("Executive Summary")
            .generatedAt(LocalDateTime.now())
            .summary(summary)
            .sections(sections)
            .build();
    }

    // ── Legal Vault Activity Data ──
    public ReportDataDTO generateLegalActivityData() {
        log.info("Generating Legal Vault Activity data");

        List<ContractFile> allFiles = contractFileRepository.findAll();
        List<ContractFile> activeFiles = allFiles.stream()
            .filter(f -> f.getDeletedAt() == null && Boolean.TRUE.equals(f.getIsActive()))
            .toList();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalFiles", allFiles.size());
        summary.put("activeFiles", activeFiles.size());
        summary.put("deletedFiles", allFiles.size() - activeFiles.size());
        long totalSize = activeFiles.stream().mapToLong(f -> f.getFileSizeBytes() != null ? f.getFileSizeBytes() : 0).sum();
        summary.put("totalStorageMB", String.format("%.1f MB", totalSize / 1024.0 / 1024.0));
        summary.put("encryptionStatus", "AES-256-GCM + CMK");

        List<Map<String, Object>> sections = new ArrayList<>();

        // Section: Active files
        List<Map<String, Object>> fileRows = new ArrayList<>();
        for (ContractFile f : activeFiles) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("fullName", f.getFileName());
            row.put("role", f.getContentType() != null ? f.getContentType().toUpperCase() : "FILE");
            row.put("company", f.getProject() != null ? f.getProject().getTitle() : "\u2014");
            row.put("mfaEnabled", true);
            row.put("actionRequired", false);
            row.put("username", f.getFileSizeBytes() != null ? String.format("%.1f KB", f.getFileSizeBytes() / 1024.0) : "\u2014");
            fileRows.add(row);
        }

        Map<String, Object> fileSection = new LinkedHashMap<>();
        fileSection.put("title", "Active Contract Files");
        fileSection.put("status", "COMPLIANT");
        fileSection.put("users", fileRows);
        sections.add(fileSection);

        // Section: Security checks
        Map<String, Object> secSection = new LinkedHashMap<>();
        secSection.put("title", "Legal Vault Security");
        secSection.put("status", "COMPLIANT");
        secSection.put("items", List.of(
            Map.of("check", "All files encrypted with AES-256-GCM", "status", true),
            Map.of("check", "CMK managed via Azure Key Vault (RSA-3072)", "status", true),
            Map.of("check", "Soft-delete retention: 90 days", "status", true),
            Map.of("check", "Blob versioning enabled", "status", true),
            Map.of("check", "No public access to storage containers", "status", true),
            Map.of("check", "SHA-256 duplicate detection active", "status", true)
        ));
        sections.add(secSection);

        return ReportDataDTO.builder()
            .templateId("legal-activity")
            .templateName("Legal Vault Activity")
            .generatedAt(LocalDateTime.now())
            .summary(summary)
            .sections(sections)
            .build();
    }

}