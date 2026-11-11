package com.next2me.next2view.service;

import com.next2me.next2view.dto.ReportDataDTO;
import com.next2me.next2view.model.Company;
import com.next2me.next2view.model.Project;
import com.next2me.next2view.model.User;
import com.next2me.next2view.repository.CompanyRepository;
import com.next2me.next2view.repository.ContractFileRepository;
import com.next2me.next2view.repository.ProjectRepository;
import com.next2me.next2view.repository.ReportGenerationLogRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * v5.18.0: exec summary covers only projects the actor can read, and money
 * totals appear only when the actor has viewFinancials.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceExecSummaryTest {

    @Mock UserRepository userRepository;
    @Mock ProjectRepository projectRepository;
    @Mock CompanyRepository companyRepository;
    @Mock ContractFileRepository contractFileRepository;
    @Mock ReportGenerationLogRepository reportLogRepository;
    @Mock PermissionEvaluator permissions;

    @InjectMocks ReportService service;

    private User actor;
    private Project readable;
    private Project hidden;

    @BeforeEach
    void setUp() {
        actor = User.builder().fullName("A").email("a@example.test").role(User.Role.CEO).build();
        actor.setId(UUID.randomUUID());

        Company c = Company.builder().name("Acme").code("AC").color("#000").build();
        c.setId(UUID.randomUUID());

        readable = Project.builder().title("Readable").company(c).category(Project.Category.dev)
                .budget(new BigDecimal("1000")).paid(new BigDecimal("100")).invoiced(new BigDecimal("200")).build();
        readable.setId(UUID.randomUUID());
        hidden = Project.builder().title("Hidden").company(c).category(Project.Category.legal)
                .budget(new BigDecimal("9000")).paid(new BigDecimal("900")).invoiced(new BigDecimal("800")).build();
        hidden.setId(UUID.randomUUID());

        when(projectRepository.findAllByActiveTrueOrderByUpdatedAtDesc()).thenReturn(List.of(readable, hidden));
        when(companyRepository.findAllByActiveTrueOrderByName()).thenReturn(List.of(c));
        when(permissions.canRead(actor, readable)).thenReturn(true);
        when(permissions.canRead(actor, hidden)).thenReturn(false);
    }

    @Test
    void withoutViewFinancials_noMoneyKeys() {
        when(permissions.canViewFinancials(actor)).thenReturn(false);

        ReportDataDTO data = service.generateExecSummaryData(actor);

        assertThat(data.getSummary())
                .doesNotContainKeys("totalBudget", "totalPaid", "totalInvoiced")
                .containsEntry("totalProjects", 1);
    }

    @Test
    void withViewFinancials_totalsOnlyOverReadableProjects() {
        when(permissions.canViewFinancials(actor)).thenReturn(true);

        ReportDataDTO data = service.generateExecSummaryData(actor);

        assertThat(data.getSummary())
                .containsEntry("totalProjects", 1)
                .containsEntry("totalBudget", "€1000")
                .containsEntry("totalPaid", "€100")
                .containsEntry("totalInvoiced", "€200");
    }

    @Test
    void sectionsExcludeUnreadableProjects() {
        when(permissions.canViewFinancials(actor)).thenReturn(true);

        ReportDataDTO data = service.generateExecSummaryData(actor);

        assertThat(data.getSections()).hasSize(1);
        assertThat(data.getSections().get(0).get("title").toString()).startsWith("Dev");
    }

    @Test
    void previewRouter_threadsActor() {
        when(permissions.canViewFinancials(actor)).thenReturn(false);

        ReportDataDTO data = service.generatePreviewData("exec-summary", actor);

        assertThat(data.getSummary()).doesNotContainKey("totalBudget");
    }
}
