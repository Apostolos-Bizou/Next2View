package com.next2me.next2view.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.next2me.next2view.dto.ProjectDto;
import com.next2me.next2view.dto.ProjectRequest;
import com.next2me.next2view.model.Company;
import com.next2me.next2view.model.Project;
import com.next2me.next2view.model.User;
import com.next2me.next2view.repository.AuditLogRepository;
import com.next2me.next2view.repository.CompanyRepository;
import com.next2me.next2view.repository.ProjectRepository;
import com.next2me.next2view.repository.UserRepository;
import com.next2me.next2view.security.PermissionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

/**
 * v5.18.0: money fields (budget / paid / invoiced) are masked in every ProjectDto
 * for callers without viewFinancials, and such callers can never change or erase them.
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceFinancialsTest {

    @Mock ProjectRepository projectRepository;
    @Mock CompanyRepository companyRepository;
    @Mock UserRepository userRepository;
    @Mock AuditLogRepository auditLogRepository;
    @Mock ActivityLogService activityLogService;
    @Mock PermissionEvaluator permissions;

    @InjectMocks ProjectService service;

    private static final BigDecimal BUDGET   = new BigDecimal("50000");
    private static final BigDecimal PAID     = new BigDecimal("20000");
    private static final BigDecimal INVOICED = new BigDecimal("30000");

    private User actor;
    private Company company;
    private Project project;

    @BeforeEach
    void setUp() {
        actor = User.builder().fullName("Dept Head").email("dh@example.test")
                .role(User.Role.DEPT_HEAD).department(User.Department.dev).build();
        actor.setId(UUID.randomUUID());

        company = Company.builder().name("Acme").code("AC").color("#000").build();
        company.setId(UUID.randomUUID());

        project = Project.builder().title("P1").company(company)
                .category(Project.Category.dev)
                .budget(BUDGET).paid(PAID).invoiced(INVOICED).build();
        project.setId(UUID.randomUUID());

        when(permissions.requireUser(actor.getId())).thenReturn(actor);
    }

    private ProjectRequest request(BigDecimal budget, BigDecimal paid, BigDecimal invoiced) {
        return new ProjectRequest("P1", company.getId(), Project.Category.dev,
                budget, paid, invoiced, null, null, null, null,
                List.of(), List.of(), null);
    }

    // ── DTO masking ──

    @Test
    void findById_withViewFinancials_returnsMoney() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(permissions.canViewFinancials(actor)).thenReturn(true);

        ProjectDto dto = service.findById(project.getId(), actor.getId());

        assertThat(dto.budget()).isEqualByComparingTo(BUDGET);
        assertThat(dto.paid()).isEqualByComparingTo(PAID);
        assertThat(dto.invoiced()).isEqualByComparingTo(INVOICED);
    }

    @Test
    void findById_withoutViewFinancials_masksMoney_andJsonOmitsKeys() throws Exception {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(permissions.canViewFinancials(actor)).thenReturn(false);

        ProjectDto dto = service.findById(project.getId(), actor.getId());

        assertThat(dto.budget()).isNull();
        assertThat(dto.paid()).isNull();
        assertThat(dto.invoiced()).isNull();

        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(dto);
        assertThat(json).doesNotContain("\"budget\"").doesNotContain("\"paid\"").doesNotContain("\"invoiced\"");
        assertThat(json).contains("\"title\":\"P1\"");
    }

    @Test
    void findAll_withoutViewFinancials_masksMoneyOnEveryRow() {
        when(permissions.isCeo(actor)).thenReturn(false);
        when(permissions.scopedCompanyId(actor)).thenReturn(null);
        when(permissions.allowedCategories(actor)).thenReturn(Set.of(Project.Category.dev));
        when(permissions.allowedCompanyIds(actor)).thenReturn(Set.of());
        when(permissions.allowedProjectIds(actor)).thenReturn(Set.of());
        when(permissions.canViewFinancials(actor)).thenReturn(false);
        when(projectRepository.findForScope(any(), anyBoolean(), anySet())).thenReturn(List.of(project));

        List<ProjectDto> rows = service.findAll(actor.getId(), null, null);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).budget()).isNull();
        assertThat(rows.get(0).paid()).isNull();
        assertThat(rows.get(0).invoiced()).isNull();
    }

    @Test
    void findById_ceo_alwaysSeesMoney() {
        // CEO path: PermissionEvaluator.canViewFinancials returns true for CEO
        // (covered directly in PermissionEvaluatorTest); service just honours it.
        actor.setRole(User.Role.CEO);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(permissions.canViewFinancials(actor)).thenReturn(true);

        ProjectDto dto = service.findById(project.getId(), actor.getId());

        assertThat(dto.budget()).isEqualByComparingTo(BUDGET);
    }

    // ── Write guard ──

    @Test
    void update_withoutViewFinancials_sendingBudgetZero_keepsStoredMoney() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        when(permissions.canViewFinancials(actor)).thenReturn(false);

        // Exactly what a stale bundle sends: budget:0, paid/invoiced absent
        ProjectDto dto = service.update(project.getId(), request(BigDecimal.ZERO, null, null),
                actor.getId(), actor.getEmail());

        assertThat(project.getBudget()).isEqualByComparingTo(BUDGET);
        assertThat(project.getPaid()).isEqualByComparingTo(PAID);
        assertThat(project.getInvoiced()).isEqualByComparingTo(INVOICED);
        assertThat(dto.budget()).isNull();
    }

    @Test
    void update_withoutViewFinancials_sendingAllMoney_keepsStoredMoney() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        when(permissions.canViewFinancials(actor)).thenReturn(false);

        service.update(project.getId(), request(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")),
                actor.getId(), actor.getEmail());

        assertThat(project.getBudget()).isEqualByComparingTo(BUDGET);
        assertThat(project.getPaid()).isEqualByComparingTo(PAID);
        assertThat(project.getInvoiced()).isEqualByComparingTo(INVOICED);
    }

    @Test
    void update_withViewFinancials_changesBudget_nullsKeepStored() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        when(permissions.canViewFinancials(actor)).thenReturn(true);

        ProjectDto dto = service.update(project.getId(), request(new BigDecimal("75000"), null, null),
                actor.getId(), actor.getEmail());

        assertThat(project.getBudget()).isEqualByComparingTo("75000");
        assertThat(project.getPaid()).isEqualByComparingTo(PAID);
        assertThat(project.getInvoiced()).isEqualByComparingTo(INVOICED);
        assertThat(dto.budget()).isEqualByComparingTo("75000");
    }

    @Test
    void update_withViewFinancials_nullBudget_doesNotClear() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        when(permissions.canViewFinancials(actor)).thenReturn(true);

        service.update(project.getId(), request(null, null, null), actor.getId(), actor.getEmail());

        assertThat(project.getBudget()).isEqualByComparingTo(BUDGET);
    }

    @Test
    void create_withoutViewFinancials_ignoresMoney() {
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        when(permissions.canViewFinancials(actor)).thenReturn(false);

        ProjectDto dto = service.create(request(new BigDecimal("999"), new BigDecimal("5"), new BigDecimal("6")),
                actor.getId(), actor.getEmail());

        org.mockito.ArgumentCaptor<Project> saved = org.mockito.ArgumentCaptor.forClass(Project.class);
        org.mockito.Mockito.verify(projectRepository).save(saved.capture());
        assertThat(saved.getValue().getBudget()).isNull();
        assertThat(saved.getValue().getPaid()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getValue().getInvoiced()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.budget()).isNull();
    }

    @Test
    void create_withViewFinancials_storesMoney() {
        when(companyRepository.findById(company.getId())).thenReturn(Optional.of(company));
        when(permissions.canViewFinancials(actor)).thenReturn(true);

        ProjectDto dto = service.create(request(BUDGET, PAID, INVOICED), actor.getId(), actor.getEmail());

        assertThat(dto.budget()).isEqualByComparingTo(BUDGET);
        assertThat(dto.paid()).isEqualByComparingTo(PAID);
        assertThat(dto.invoiced()).isEqualByComparingTo(INVOICED);
    }
}
