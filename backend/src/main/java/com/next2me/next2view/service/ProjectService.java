package com.next2me.next2view.service;

import com.next2me.next2view.dto.ProjectDto;
import com.next2me.next2view.dto.ProjectRequest;
import com.next2me.next2view.model.*;
import com.next2me.next2view.repository.*;
import com.next2me.next2view.service.ActivityLogService;
import com.next2me.next2view.security.PermissionEvaluator;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.LinkedHashMap;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ActivityLogService activityLogService;
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
                .description(req.description())
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
        Map<String, TaskSnapshot> oldTasks = snapshotTasks(p);
        String oldDescription = p.getDescription();

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
        p.setDescription(req.description());
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

        activityLogService.logActivity(actor, ActivityLogService.UPDATED, ActivityLogService.PROJECT,
                p.getId(), p.getTitle(), p.getCategory().name(),
                p.getCompany().getId(), actor.getFullName() + " updated project '" + p.getTitle() + "'");

        // Description-level logging (separate audit trail for permanent project description)
        String newDescription = p.getDescription();
        if (!java.util.Objects.equals(
                oldDescription == null ? "" : oldDescription,
                newDescription == null ? "" : newDescription)) {
            Map<String, Object> descMetadata = new LinkedHashMap<>();
            descMetadata.put("oldDescription", oldDescription == null ? "" : oldDescription);
            descMetadata.put("newDescription", newDescription == null ? "" : newDescription);
            activityLogService.logActivity(actor,
                    ActivityLogService.DESCRIPTION_UPDATED, ActivityLogService.PROJECT,
                    p.getId(), p.getTitle(), p.getCategory().name(),
                    p.getCompany().getId(),
                    actor.getFullName() + " updated description of project '" + p.getTitle() + "'",
                    descMetadata);
        }

        // Granular task-level logging
        logTaskDiffs(actor, p, oldTasks);

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

        activityLogService.logActivity(actor, ActivityLogService.DELETED, ActivityLogService.PROJECT,
                p.getId(), p.getTitle(), p.getCategory().name(),
                p.getCompany().getId(), actor.getFullName() + " deleted project '" + p.getTitle() + "'");
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
                    t.setDescription(tr.description());
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
                    t.getComment(), t.getDescription(), t.getDeadline(), t.getStartWeek(), t.getDurationWeeks(), t.getStartDay(), t.getDurationDays(), t.getManualProgress(), t.getStartDate(), t.getEndDate()
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
                p.getDescription(),
                completion, (int) tasksTotal, (int) tasksDone,
                updatedAgo, modules, specs
        );
    }

    // ── Task-level diff support ──

    private record TaskSnapshot(String name, String assignee, int progress, boolean isDone, boolean isBlocked, String moduleName, String description) {}

    private Map<String, TaskSnapshot> snapshotTasks(Project p) {
        Map<String, TaskSnapshot> map = new LinkedHashMap<>();
        for (var m : p.getModules()) {
            for (var t : m.getTasks()) {
                String key = m.getName() + "::" + t.getName();
                map.put(key, new TaskSnapshot(t.getName(), t.getAssignee(), t.getProgress(), t.getIsDone(), t.getIsBlocked(), m.getName(), t.getDescription()));
            }
        }
        return map;
    }

    private void logTaskDiffs(User actor, Project p, Map<String, TaskSnapshot> oldTasks) {
        Map<String, TaskSnapshot> newTasks = snapshotTasks(p);
        String projectTitle = p.getTitle();
        UUID projectId = p.getId();
        String category = p.getCategory().name();
        UUID companyId = p.getCompany().getId();
        String actorName = actor.getFullName();

        // New tasks (in new but not in old)
        for (var entry : newTasks.entrySet()) {
            if (!oldTasks.containsKey(entry.getKey())) {
                TaskSnapshot ts = entry.getValue();
                activityLogService.logActivity(actor, "TASK_ADDED", ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " added task '" + ts.name() + "' in " + ts.moduleName());
            }
        }

        // Deleted tasks (in old but not in new)
        for (var entry : oldTasks.entrySet()) {
            if (!newTasks.containsKey(entry.getKey())) {
                TaskSnapshot ts = entry.getValue();
                activityLogService.logActivity(actor, "TASK_REMOVED", ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " removed task '" + ts.name() + "' from " + ts.moduleName());
            }
        }

        // Changed tasks (in both — check diffs)
        for (var entry : newTasks.entrySet()) {
            if (!oldTasks.containsKey(entry.getKey())) continue;
            TaskSnapshot oldT = oldTasks.get(entry.getKey());
            TaskSnapshot newT = entry.getValue();

            // Completed
            if (!oldT.isDone() && newT.isDone()) {
                activityLogService.logActivity(actor, "TASK_COMPLETED", ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " completed task '" + newT.name() + "' in " + newT.moduleName());
            }
            // Uncompleted (was done, now not)
            else if (oldT.isDone() && !newT.isDone()) {
                activityLogService.logActivity(actor, "TASK_REOPENED", ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " reopened task '" + newT.name() + "' in " + newT.moduleName());
            }
            // Progress changed (only if not a completion/reopen)
            else if (oldT.progress() != newT.progress()) {
                activityLogService.logActivity(actor, "TASK_PROGRESS", ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " changed progress of '" + newT.name() + "' from " + oldT.progress() + "% to " + newT.progress() + "% in " + newT.moduleName());
            }

            // Description changed
            if (!java.util.Objects.equals(
                    oldT.description() == null ? "" : oldT.description(),
                    newT.description() == null ? "" : newT.description())) {
                Map<String, Object> taskDescMeta = new LinkedHashMap<>();
                taskDescMeta.put("taskName", newT.name());
                taskDescMeta.put("moduleName", newT.moduleName());
                taskDescMeta.put("oldDescription", oldT.description() == null ? "" : oldT.description());
                taskDescMeta.put("newDescription", newT.description() == null ? "" : newT.description());
                activityLogService.logActivity(actor,
                        ActivityLogService.TASK_DESCRIPTION_UPDATED, ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " updated description of task '" + newT.name() + "' in " + newT.moduleName(),
                        taskDescMeta);
            }

            // Assignee changed
            if (!java.util.Objects.equals(oldT.assignee(), newT.assignee())) {
                String from = oldT.assignee() != null ? oldT.assignee() : "unassigned";
                String to = newT.assignee() != null ? newT.assignee() : "unassigned";
                activityLogService.logActivity(actor, "TASK_REASSIGNED", ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " reassigned '" + newT.name() + "' from " + from + " to " + to + " in " + newT.moduleName());
            }

            // Blocked/unblocked
            if (!oldT.isBlocked() && newT.isBlocked()) {
                activityLogService.logActivity(actor, "TASK_BLOCKED", ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " blocked task '" + newT.name() + "' in " + newT.moduleName());
            } else if (oldT.isBlocked() && !newT.isBlocked()) {
                activityLogService.logActivity(actor, "TASK_UNBLOCKED", ActivityLogService.PROJECT,
                        projectId, projectTitle, category, companyId,
                        actorName + " unblocked task '" + newT.name() + "' in " + newT.moduleName());
            }
        }
    }

}
