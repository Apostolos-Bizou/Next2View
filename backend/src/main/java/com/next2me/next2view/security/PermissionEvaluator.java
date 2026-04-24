package com.next2me.next2view.security;

import com.next2me.next2view.model.Project;
import com.next2me.next2view.model.User;
import com.next2me.next2view.model.UserPermission;
import com.next2me.next2view.repository.UserPermissionRepository;
import com.next2me.next2view.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Centralized permission evaluation for Next2View.
 *
 * Model:
 *   - CEO: full access (bypass all checks)
 *   - DEPT_HEAD: base access = user.company + user.department
 *                extended access = base + categories granted via UserPermission flags
 *                HOWEVER, extended categories are ALWAYS scoped to user.company
 *   - VIEWER: (reserved; treated same as DEPT_HEAD for now, but enforced read-only)
 *
 * Strict fail policy: a DEPT_HEAD without company or department gets 403.
 */
@Component
@RequiredArgsConstructor
public class PermissionEvaluator {

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;

    public User requireUser(UUID userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public boolean isCeo(User user) {
        return user != null && user.getRole() == User.Role.CEO;
    }

    /**
     * Returns the set of categories this user is allowed to see.
     * CEO: all categories. DEPT_HEAD/VIEWER: department + extensions from UserPermission.
     * Throws 403 if user is non-CEO and has no company or no department.
     */
    public Set<Project.Category> allowedCategories(User user) {
        if (isCeo(user)) {
            return EnumSet.allOf(Project.Category.class);
        }

        // Strict fail: DEPT_HEAD / VIEWER must have a department set.
        // Company is NOT required anymore (cross-company access by department).
        if (user.getDepartment() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Your account is missing department. Contact the CEO.");
        }

        Set<Project.Category> allowed = EnumSet.noneOf(Project.Category.class);

        // Base: map department -> category (management is not a project category, so skipped)
        Project.Category baseCategory = mapDepartmentToCategory(user.getDepartment());
        if (baseCategory != null) {
            allowed.add(baseCategory);
        }

        // Extensions from UserPermission (System B)
        userPermissionRepository.findByUserId(user.getId()).ifPresent(perm -> {
            if (Boolean.TRUE.equals(perm.getViewFinance()))   allowed.add(Project.Category.finance);
            if (Boolean.TRUE.equals(perm.getViewLegal()))     allowed.add(Project.Category.legal);
            if (Boolean.TRUE.equals(perm.getViewDev()))       allowed.add(Project.Category.dev);
            if (Boolean.TRUE.equals(perm.getViewMarketing())) allowed.add(Project.Category.marketing);
        });

        // If department is 'management' and no extensions, allowed is empty - still a valid state
        // but user will see no projects. This is intentional: management users need explicit grants.
        return allowed;
    }

    /**
     * Returns the company the user is scoped to. Always null now — DEPT_HEAD and VIEWER
     * access projects cross-company, scoped only by department/category.
     * The user.company field is retained as HR metadata (which company employs them)
     * but does not restrict project visibility.
     */
    public UUID scopedCompanyId(User user) {
        return null;
    }

    /**
     * Can this user READ this project?
     */
    public boolean canRead(User user, Project project) {
        if (isCeo(user)) return true;
        if (project == null) return false;
        // No company restriction: access is determined by department/category only.
        return allowedCategories(user).contains(project.getCategory());
    }

    /**
     * Can this user MODIFY (update/delete tasks, upload, edit) this project?
     * Currently: same rules as read, but VIEWER (if introduced) would be read-only.
     */
    public boolean canWrite(User user, Project project) {
        if (user == null) return false;
        if (user.getRole() == User.Role.VIEWER) return false;
        return canRead(user, project);
    }

    /**
     * Can this user CREATE a new project with the given company and category?
     */
    public boolean canCreateInScope(User user, UUID targetCompanyId, Project.Category targetCategory) {
        if (isCeo(user)) return true;
        if (user.getRole() == User.Role.VIEWER) return false;
        if (targetCompanyId == null || targetCategory == null) return false;
        // No company restriction: DEPT_HEAD can create projects for any company,
        // as long as the category falls within their allowed categories.
        return allowedCategories(user).contains(targetCategory);
    }

    public void requireMfaForLegal(Project project, boolean mfaVerified) {
            if (project != null && project.getCategory() == Project.Category.legal && !mfaVerified) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "MFA verification required to access legal documents");
            }
        }
    
        public void requireCanRead(User user, Project project) {
        if (!canRead(user, project)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this project");
        }
    }

    public void requireCanWrite(User user, Project project) {
        if (!canWrite(user, project)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Write access denied to this project");
        }
    }

    public void requireCanCreate(User user, UUID targetCompanyId, Project.Category targetCategory) {
        if (!canCreateInScope(user, targetCompanyId, targetCategory)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cannot create project outside your company and department");
        }
    }

    private Project.Category mapDepartmentToCategory(User.Department dept) {
        if (dept == null) return null;
        return switch (dept) {
            case finance   -> Project.Category.finance;
            case legal     -> Project.Category.legal;
            case dev       -> Project.Category.dev;
            case marketing -> Project.Category.marketing;
            case management -> null; // management is not a project category
        };
    }

    // ═══════════════════════════════════════════════════════════════
    // Permission-flag based checks (called from @PreAuthorize)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Used by @PreAuthorize as: hasRole('CEO') or @permissionEvaluator.canManageCompanies(principal)
     * Returns true if the user has manageCompanies permission flag.
     */
    public boolean canManageCompanies(String userId) {
        if (userId == null) return false;
        try {
            User user = requireUser(UUID.fromString(userId));
            if (isCeo(user)) return true;
            if (user.getRole() == User.Role.VIEWER) return false;
            return userPermissionRepository.findByUserId(user.getId())
                    .map(p -> Boolean.TRUE.equals(p.getManageCompanies()))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Used by @PreAuthorize as: hasRole('CEO') or @permissionEvaluator.canManageUsers(principal)
     */
    public boolean canManageUsers(String userId) {
        if (userId == null) return false;
        try {
            User user = requireUser(UUID.fromString(userId));
            if (isCeo(user)) return true;
            if (user.getRole() == User.Role.VIEWER) return false;
            return userPermissionRepository.findByUserId(user.getId())
                    .map(p -> Boolean.TRUE.equals(p.getManageUsers()))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Used by @PreAuthorize as: hasRole('CEO') or @permissionEvaluator.canCreateProject(principal)
     */
    public boolean canCreateProject(String userId) {
        if (userId == null) return false;
        try {
            User user = requireUser(UUID.fromString(userId));
            if (isCeo(user)) return true;
            if (user.getRole() == User.Role.VIEWER) return false;
            return userPermissionRepository.findByUserId(user.getId())
                    .map(p -> Boolean.TRUE.equals(p.getCreateProject()))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
}
