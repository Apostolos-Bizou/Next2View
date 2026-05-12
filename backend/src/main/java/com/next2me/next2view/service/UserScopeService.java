package com.next2me.next2view.service;

import com.next2me.next2view.dto.UserScopeDto;
import com.next2me.next2view.model.Company;
import com.next2me.next2view.model.Project;
import com.next2me.next2view.model.User;
import com.next2me.next2view.model.UserCompanyScope;
import com.next2me.next2view.model.UserProjectScope;
import com.next2me.next2view.repository.CompanyRepository;
import com.next2me.next2view.repository.ProjectRepository;
import com.next2me.next2view.repository.UserCompanyScopeRepository;
import com.next2me.next2view.repository.UserProjectScopeRepository;
import com.next2me.next2view.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-user company and project scopes (opt-in restrictions).
 * Empty scope lists = no restriction (default).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserScopeService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final UserCompanyScopeRepository userCompanyScopeRepository;
    private final UserProjectScopeRepository userProjectScopeRepository;
    private final ActivityLogService activityLogService;

    /**
     * Returns the current scope configuration for the given user.
     * For CEO users, returns empty lists (CEO is never scope-restricted).
     */
    @Transactional(readOnly = true)
    public UserScopeDto getForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() == User.Role.CEO) {
            return new UserScopeDto(List.of(), List.of());
        }

        List<UUID> companies = userCompanyScopeRepository.findByUserId(userId).stream()
                .map(s -> s.getCompany().getId())
                .toList();
        List<UUID> projects = userProjectScopeRepository.findByUserId(userId).stream()
                .map(s -> s.getProject().getId())
                .toList();
        return new UserScopeDto(companies, projects);
    }

    /**
     * Replaces both scope lists atomically. Sending empty lists removes all restrictions.
     * No-op for CEO users (always returns empty).
     */
    @Transactional
    public UserScopeDto setForUser(UUID userId, UserScopeDto dto, User actor) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (target.getRole() == User.Role.CEO) {
            log.info("Scope update requested for CEO user {} - no-op", target.getEmail());
            return new UserScopeDto(List.of(), List.of());
        }

        List<UUID> requestedCompanies = dto.companyScope() != null ? dto.companyScope() : List.of();
        List<UUID> requestedProjects = dto.projectScope() != null ? dto.projectScope() : List.of();

        // Replace company scope
        userCompanyScopeRepository.deleteByUserId(userId);
        userCompanyScopeRepository.flush();
        List<UserCompanyScope> newCompanyScopes = new ArrayList<>();
        for (UUID companyId : requestedCompanies) {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found: " + companyId));
            newCompanyScopes.add(UserCompanyScope.builder()
                    .user(target)
                    .company(company)
                    .createdBy(actor)
                    .build());
        }
        if (!newCompanyScopes.isEmpty()) {
            userCompanyScopeRepository.saveAll(newCompanyScopes);
        }

        // Replace project scope
        userProjectScopeRepository.deleteByUserId(userId);
        userProjectScopeRepository.flush();
        List<UserProjectScope> newProjectScopes = new ArrayList<>();
        for (UUID projectId : requestedProjects) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project not found: " + projectId));
            newProjectScopes.add(UserProjectScope.builder()
                    .user(target)
                    .project(project)
                    .createdBy(actor)
                    .build());
        }
        if (!newProjectScopes.isEmpty()) {
            userProjectScopeRepository.saveAll(newProjectScopes);
        }

        // Activity log
        Map<String, Object> metadata = Map.of(
                "companyScopeSize", requestedCompanies.size(),
                "projectScopeSize", requestedProjects.size()
        );
        activityLogService.logActivity(actor,
                ActivityLogService.USER_SCOPE_UPDATED,
                ActivityLogService.USER,
                target.getId(),
                target.getFullName(),
                null, null,
                "Updated scope for " + target.getFullName() + " (" + requestedCompanies.size() + " companies, " + requestedProjects.size() + " projects)",
                metadata);

        log.info("Scope updated for user {} by {}: {} companies, {} projects",
                target.getEmail(), actor.getEmail(), requestedCompanies.size(), requestedProjects.size());

        return new UserScopeDto(requestedCompanies, requestedProjects);
    }
}
