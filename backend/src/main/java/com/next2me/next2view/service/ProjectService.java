package com.next2me.next2view.service;

import com.next2me.next2view.dto.ProjectDto;
import com.next2me.next2view.dto.ProjectRequest;
import com.next2me.next2view.model.*;
import com.next2me.next2view.repository.*;
import com.next2me.next2view.security.PermissionEvaluator;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PermissionEvaluator permissions;

    @Transactional(readOnly = true)
    public List<ProjectDto> findAll(UUID actorId, UUID requestedCompanyId, Project.Category requestedCategory) {
        User actor = permissions.requireUser(actorId);
        UUID scopedCompany;
        java.util.Set<Project.Category> allowedCats;

        if (permissions.isCeo(actor)) {
            // CEO: honor optional filters as-is
            scopedCompany = requestedCompanyId;
            allowedCats = (requestedCategory != null)
                    ? java.util.EnumSet.of(requestedCategory)
                    : java.util.EnumSet.allOf(Project.Category.class);
        } else {
            // DEPT_HEAD / VIEWER: force scope to user.company, intersect categories
            scopedCompany = permissions.scopedCompanyId(actor);
            java.util.Set<Project.Category> userAllowed = permissions.allowedCategories(actor);
            if (requestedCategory != null) {
                if (!userAllowed.contains(requestedCategory)) {
                    return java.util.List.of();
                }
                allowedCats = java.util.EnumSet.of(requestedCategory);
            } else {
                allowedCats = userAllowed;
            }
            // If user tried to filter by a different company, ignore and return empty
            if (requestedCompanyId != null && !requestedCompanyId.equals(scopedCompany)) {
                return java.util.List.of();
            }
        }

        if (allowedCats.isEmpty()) {
            return java.util.List.of();
        }

        boolean useCatFilter = !permissions.isCeo(actor) || requestedCategory != null;
        List<Project> projects = projectRepository.findForScope(scopedCompany, useCatFilter, allowedCats);
        return projects.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ProjectDto findById(UUID id, UUID actorId) {
        User actor = permissions.requireUser(actorId);
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        permissions.requireCanRead(actor, p);
        return toDto(p);
    }

    @Transactional
    public ProjectDto create(ProjectRequest req, UUID actorId, String actorEmail) {
        User actor = permissions.requireUser(actorId);
        permissions.requireCanCreate(actor, req.companyId(), req.category());
        Company company = companyRepository.findById(req.companyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found"));

        Project p = Project.builder()
                .title(req.title())
                .company(company)
                .category(req.category())
                .budget(req.budget())
                .paid(req.paid() != null ? req.paid() : java.math.BigDecimal.ZERO)
                .invoiced(req.invoiced() != null ? req.invoiced() : java.math.BigDecimal.ZERO)
                .startDate(req.startDate())
                .deadline(req.deadline())
                .contractDesc(req.contractDesc())
                .status(Project.Status.on_track)
                .active(true)
                .createdBy(actor)
                .lastUpdatedBy(actor)
                .build();

        applySpecs(p, req.specs());
        applyModules(p, req.modules());
        projectRepository.save(p);

        auditLogRepository.save(AuditLog.builder()
                .userEmail(actorEmail).action("CREATE")
                .entityType("projects").entityId(p.getId())
                .newValue(Map.of("title", p.getTitle()))
                .build());

        return toDto(p);
    }

    @Transactional
    public ProjectDto update(UUID id, ProjectRequest req, UUID actorId, String actorEmail) {
        User actor = permissions.requireUser(actorId);
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        // Must have write access to the existing project
        permissions.requireCanWrite(actor, p);
        // And must be allowed to put it into the requested (possibly new) scope
        permissions.requireCanCreate(actor, req.companyId(), req.category());

        Map<String, Object> oldVal = Map.of("title", p.getTitle());

        Company company = companyRepository.findById(req.companyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found"));

        p.setTitle(req.title());
        p.setCompany(company);
        p.setCategory(req.category());
        p.setBudget(req.budget());
        if (req.paid() != null) p.setPaid(req.paid());
        if (req.invoiced() != null) p.setInvoiced(req.invoiced());
        p.setStartDate(req.startDate());
        p.setDeadline(req.deadline());
        p.setContractDesc(req.contractDesc());
        p.setLastUpdatedBy(actor);

        p.getSpecs().clear();
        p.getModules().clear();
        applySpecs(p, req.specs());
        applyModules(p, req.modules());

        updateStatus(p);
        projectRepository.save(p);

        auditLogRepository.save(AuditLog.builder()
                .userEmail(actorEmail).action("UPDATE")
                .entityType("projects").entityId(id)
                .oldValue(oldVal)
                .newValue(Map.of("title", p.getTitle()))
                .build());

        return toDto(p);
    }

    @Transactional
    public void delete(UUID id, UUID actorId, String actorEmail) {
        User actor = permissions.requireUser(actorId);
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        // Defense in depth: controller has @PreAuthorize hasRole(CEO), we re-check here
        if (!permissions.isCeo(actor)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only CEO can delete projects");
        }
        p.setActive(false);
        projectRepository.save(p);

        auditLogRepository.save(AuditLog.builder()
                .userEmail(actorEmail).action("DELETE")
                .entityType("projects").entityId(id)
                .oldValue(Map.of("title", p.getTitle()))
                .build());
    }

    // ── Helpers ──

    private void applySpecs(Project p, List<ProjectRequest.SpecRequest> specs) {
        if (specs == null) return;
        IntStream.range(0, specs.size()).forEach(i -> {
            var s = specs.get(i);
            ProjectSpec spec = new ProjectSpec();
            spec.setProject(p);
            spec.setDescription(s.description());
            spec.setIsDone(s.isDone());
            spec.setSortOrder(i);
            p.getSpecs().add(spec);
        });
    }

    private void applyModules(Project p, List<ProjectRequest.ModuleRequest> modules) {
        if (modules == null) return;
        IntStream.range(0, modules.size()).forEach(mi -> {
            var mr = modules.get(mi);
            com.next2me.next2view.model.Module m = new com.next2me.next2view.model.Module();
            m.setProject(p);
            m.setName(mr.name());
            m.setColor(mr.color() != null ? mr.color() : p.getCategory().name());
            m.setSortOrder(mi);
            if (mr.tasks() != null) {
                IntStream.range(0, mr.tasks().size()).forEach(ti -> {
                    var tr = mr.tasks().get(ti);
                    Task t = new Task();
                    t.setModule(m);
                    t.setName(tr.name());
                    t.setAssignee(tr.assignee());
                    t.setProgress(tr.progress());
                    t.setIsDone(tr.isDone() || tr.progress() == 100);
                    t.setIsBlocked(tr.isBlocked());
                    t.setBlockNote(tr.blockNote());
                    t.setComment(tr.comment());
                    t.setDeadline(tr.deadline());
                    t.setStartWeek(tr.startWeek());
                    t.setDurationWeeks(tr.durationWeeks() != null ? tr.durationWeeks() : 1);
                    t.setStartDay(tr.startDay());
                    t.setDurationDays(tr.durationDays());
                    t.setManualProgress(tr.manualProgress() != null ? tr.manualProgress() : false);
                    t.setStartDate(tr.startDate());
                    t.setEndDate(tr.endDate());
                    t.setSortOrder(ti);
                    m.getTasks().add(t);
                });
            }
            p.getModules().add(m);
        });
    }

    private void updateStatus(Project p) {
        boolean hasBlocked = p.getModules().stream()
                .flatMap(m -> m.getTasks().stream())
                .anyMatch(Task::getIsBlocked);
        if (hasBlocked) { p.setStatus(Project.Status.at_risk); return; }

        int completion = calcCompletion(p);
        if (p.getDeadline() != null) {
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(
                    java.time.LocalDate.now(), p.getDeadline());
            if (daysLeft < 7 && completion < 80) {
                p.setStatus(Project.Status.at_risk); return;
            }
            if (daysLeft < 14 && completion < 50) {
                p.setStatus(Project.Status.delayed); return;
            }
        }
        p.setStatus(Project.Status.on_track);
    }

    private int calcCompletion(Project p) {
        var tasks = p.getModules().stream()
                .flatMap(m -> m.getTasks().stream()).toList();
        if (tasks.isEmpty()) return 0;
        return (int) tasks.stream().mapToInt(Task::getProgress).average().orElse(0);
    }

    private ProjectDto toDto(Project p) {
        int completion = calcCompletion(p);
        long tasksTotal = p.getModules().stream().mapToLong(m -> m.getTasks().size()).sum();
        long tasksDone  = p.getModules().stream()
                .flatMap(m -> m.getTasks().stream()).filter(Task::getIsDone).count();
        long updatedAgo = p.getUpdatedAt() != null ?
                (Instant.now().getEpochSecond() - p.getUpdatedAt().getEpochSecond()) / 60 : 0;

        var modules = p.getModules().stream().map(m -> {
            int mc = m.getTasks().isEmpty() ? 0 :
                    (int) m.getTasks().stream().mapToInt(Task::getProgress).average().orElse(0);
            var tasks = m.getTasks().stream().map(t -> new ProjectDto.TaskDto(
                    t.getId(), t.getName(), t.getAssignee(), t.getProgress(),
                    t.getIsDone(), t.getIsBlocked(), t.getBlockNote(),
                    t.getComment(), t.getDeadline(), t.getStartWeek(), t.getDurationWeeks(), t.getStartDay(), t.getDurationDays(), t.getManualProgress(), t.getStartDate(), t.getEndDate()
            )).toList();
            return new ProjectDto.ModuleDto(m.getId(), m.getName(), m.getColor(), mc, tasks);
        }).toList();

        var specs = p.getSpecs().stream().map(s ->
                new ProjectDto.SpecDto(s.getId(), s.getDescription(), s.getIsDone(), s.getSortOrder(), s.getStartDate(), s.getEndDate())
        ).toList();

        return new ProjectDto(
                p.getId(), p.getTitle(),
                p.getCompany().getId(), p.getCompany().getName(),
                p.getCompany().getCode(), p.getCompany().getColor(),
                p.getCategory(), p.getStatus(),
                p.getBudget(), p.getPaid(), p.getInvoiced(),
                p.getStartDate(), p.getDeadline(), p.getContractDesc(),
                completion, (int) tasksTotal, (int) tasksDone,
                updatedAgo, modules, specs
        );
    }
}