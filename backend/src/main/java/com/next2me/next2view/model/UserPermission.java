package com.next2me.next2view.model;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "user_permissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Visibility
    @Builder.Default @Column(name = "view_finance")     private Boolean viewFinance    = false;
    @Builder.Default @Column(name = "view_legal")       private Boolean viewLegal      = false;
    @Builder.Default @Column(name = "view_dev")         private Boolean viewDev        = false;
    @Builder.Default @Column(name = "view_marketing")   private Boolean viewMarketing  = false;
    @Builder.Default @Column(name = "view_financials")  private Boolean viewFinancials = false;
    @Builder.Default @Column(name = "view_ceo_notes")   private Boolean viewCeoNotes   = false;

    // Actions
    @Builder.Default @Column(name = "update_tasks")     private Boolean updateTasks    = false;
    @Builder.Default @Column(name = "upload_files")     private Boolean uploadFiles    = false;
    @Builder.Default @Column(name = "create_project")   private Boolean createProject  = false;
    @Builder.Default @Column(name = "edit_project")     private Boolean editProject    = false;

    // Management
    @Builder.Default @Column(name = "manage_users")     private Boolean manageUsers    = false;
    @Builder.Default @Column(name = "manage_companies") private Boolean manageCompanies= false;

    // AI
    @Builder.Default @Column(name = "ai_ceo_report")   private Boolean aiCeoReport    = false;
    @Builder.Default @Column(name = "ai_contract")      private Boolean aiContract     = false;

    // Documentation access
    @Builder.Default @Column(name = "view_security") private Boolean viewSecurity  = false;
}