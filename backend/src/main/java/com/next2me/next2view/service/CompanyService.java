package com.next2me.next2view.service;

import com.next2me.next2view.dto.CompanyDto;
import com.next2me.next2view.dto.CompanyRequest;
import com.next2me.next2view.model.AuditLog;
import com.next2me.next2view.model.Company;
import com.next2me.next2view.repository.AuditLogRepository;
import com.next2me.next2view.model.Project;
import com.next2me.next2view.model.User;
import com.next2me.next2view.service.ActivityLogService;
import com.next2me.next2view.repository.CompanyRepository;
import com.next2me.next2view.repository.ProjectRepository;
import com.next2me.next2view.security.PermissionEvaluator;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final AuditLogRepository auditLogRepository;
    private final ActivityLogService activityLogService;
    private final com.next2me.next2view.repository.UserRepository userRepository;
    private final PermissionEvaluator permissions;

    @Transactional(readOnly = true)
    public List<CompanyDto> findAll(UUID actorId) {
        User actor = permissions.requireUser(actorId);
        var companies = companyRepository.findAllByActiveTrueOrderByName();

        if (permissions.isCeo(actor)) {
            // CEO sees all companies with full project counts
            return companies.stream()
                    .map(c -> toDto(c, null))
                    .toList();
        }

        // Non-CEO visibility rules:
        // 1. Companies with projects in allowed categories -> visible
        // 2. Empty companies (0 total projects) + manageCompanies -> visible
        //    (orphan protection: newly-created companies don't disappear)
        // 3. Companies with projects ONLY in other categories -> hidden
        // v5.1.2 fix: apply company + project scope to sidebar listing.
        // Mirrors the 3-layer canRead model so the sidebar reflects exactly
        // what the user is actually allowed to see (no leak of other
        // companies' existence or inflated project counts).
        java.util.Set<Project.Category> allowedCats = permissions.allowedCategories(actor);
        java.util.Set<UUID> companyScope = permissions.allowedCompanyIds(actor);
        java.util.Set<UUID> projectScope = permissions.allowedProjectIds(actor);
        boolean canManage = permissions.canManageCompanies(actorId.toString());

        return companies.stream()
                // Layer: company scope — drop companies the user is not scoped to
                .filter(c -> companyScope.isEmpty() || companyScope.contains(c.getId()))
                .map(c -> toDto(c, allowedCats, projectScope))
                .filter(dto -> {
                    if (dto.projectCount() > 0) return true;
                    if (!canManage) return false;
                    // Empty in allowed cats + canManage: show only if truly orphan
                    return projectRepository.findAllByCompanyIdAndActiveTrue(dto.id()).isEmpty();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyDto findById(UUID id) {
        Company c = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + id));
        return toDto(c);
    }

    @Transactional
    public CompanyDto create(CompanyRequest req, String actorEmail) {
        if (companyRepository.existsByCode(req.code().toUpperCase())) {
            throw new IllegalArgumentException("Company code already exists: " + req.code());
        }
        Company c = Company.builder()
                .name(req.name())
                .code(req.code().toUpperCase())
                .color(req.color())
                .description(req.description())
                .active(true)
                .build();
        companyRepository.save(c);

        auditLogRepository.save(AuditLog.builder()
                .userEmail(actorEmail)
                .action("CREATE")
                .entityType("companies")
                .entityId(c.getId())
                .newValue(Map.of("name", c.getName(), "code", c.getCode()))
                .build());

        // Activity log
        userRepository.findByEmail(actorEmail).ifPresent(actor ->
            activityLogService.logActivity(actor, ActivityLogService.CREATED, ActivityLogService.COMPANY,
                c.getId(), c.getName(), null,
                null, actor.getFullName() + " created company '" + c.getName() + "'")
        );

        return toDto(c);
    }

    @Transactional
    public CompanyDto update(UUID id, CompanyRequest req, String actorEmail) {
        Company c = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + id));

        Map<String, Object> oldVal = Map.of("name", c.getName(), "color", c.getColor());

        c.setName(req.name());
        c.setColor(req.color());
        c.setDescription(req.description());
        companyRepository.save(c);

        auditLogRepository.save(AuditLog.builder()
                .userEmail(actorEmail)
                .action("UPDATE")
                .entityType("companies")
                .entityId(id)
                .oldValue(oldVal)
                .newValue(Map.of("name", c.getName(), "color", c.getColor()))
                .build());

        // Activity log
        userRepository.findByEmail(actorEmail).ifPresent(actor ->
            activityLogService.logActivity(actor, ActivityLogService.UPDATED, ActivityLogService.COMPANY,
                c.getId(), c.getName(), null,
                null, actor.getFullName() + " updated company '" + c.getName() + "'")
        );

        return toDto(c);
    }

    @Transactional
    public void delete(UUID id, String actorEmail) {
        Company c = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + id));
        c.setActive(false);
        companyRepository.save(c);

        auditLogRepository.save(AuditLog.builder()
                .userEmail(actorEmail)
                .action("DELETE")
                .entityType("companies")
                .entityId(id)
                .oldValue(Map.of("name", c.getName()))
                .build());

        // Activity log
        userRepository.findByEmail(actorEmail).ifPresent(actor ->
            activityLogService.logActivity(actor, ActivityLogService.DELETED, ActivityLogService.COMPANY,
                c.getId(), c.getName(), null,
                null, actor.getFullName() + " deleted company '" + c.getName() + "'")
        );
    }

    // v5.1.2: overload that also applies project scope to the visible project list.
    // projectScope empty = no project-level restriction (uses categoryFilter only).
    private CompanyDto toDto(Company c, java.util.Set<Project.Category> categoryFilter, java.util.Set<UUID> projectScope) {
        var allProjects = projectRepository.findAllByCompanyIdAndActiveTrue(c.getId());
        var projects = allProjects.stream()
                .filter(p -> categoryFilter == null || categoryFilter.contains(p.getCategory()))
                .filter(p -> projectScope == null || projectScope.isEmpty() || projectScope.contains(p.getId()))
                .toList();
        int avg = projects.isEmpty() ? 0 :
                (int) projects.stream()
                        .mapToInt(p -> {
                            var mods = p.getModules();
                            if (mods.isEmpty()) return 0;
                            return (int) mods.stream()
                                    .flatMap(m -> m.getTasks().stream())
                                    .mapToInt(t -> t.getProgress())
                                    .average().orElse(0);
                        })
                        .average().orElse(0);
        return new CompanyDto(
                c.getId(), c.getName(), c.getCode(),
                c.getColor(), c.getDescription(),
                projects.size(), avg
        );
    }

    private CompanyDto toDto(Company c) {
        return toDto(c, null);
    }

    private CompanyDto toDto(Company c, java.util.Set<Project.Category> categoryFilter) {
        var allProjects = projectRepository.findAllByCompanyIdAndActiveTrue(c.getId());
        var projects = (categoryFilter == null)
                ? allProjects
                : allProjects.stream().filter(p -> categoryFilter.contains(p.getCategory())).toList();
        int avg = projects.isEmpty() ? 0 :
                (int) projects.stream()
                        .mapToInt(p -> {
                            var mods = p.getModules();
                            if (mods.isEmpty()) return 0;
                            return (int) mods.stream()
                                    .flatMap(m -> m.getTasks().stream())
                                    .mapToInt(t -> t.getProgress())
                                    .average().orElse(0);
                        })
                        .average().orElse(0);

        return new CompanyDto(
                c.getId(), c.getName(), c.getCode(),
                c.getColor(), c.getDescription(),
                projects.size(), avg
        );
    }
}