package com.next2me.next2view.security;

import com.next2me.next2view.model.User;
import com.next2me.next2view.model.UserPermission;
import com.next2me.next2view.repository.UserCompanyScopeRepository;
import com.next2me.next2view.repository.UserPermissionRepository;
import com.next2me.next2view.repository.UserProjectScopeRepository;
import com.next2me.next2view.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionEvaluatorTest {

    @Mock UserRepository userRepository;
    @Mock UserPermissionRepository userPermissionRepository;
    @Mock UserCompanyScopeRepository userCompanyScopeRepository;
    @Mock UserProjectScopeRepository userProjectScopeRepository;

    @InjectMocks PermissionEvaluator evaluator;

    private User user(User.Role role) {
        User u = User.builder().fullName("U").email("u@example.test")
                .role(role).department(User.Department.dev).build();
        u.setId(UUID.randomUUID());
        return u;
    }

    @Test
    void canViewFinancials_ceo_trueWithoutPermissionRow() {
        User ceo = user(User.Role.CEO);
        assertThat(evaluator.canViewFinancials(ceo)).isTrue();
        verify(userPermissionRepository, never()).findByUserId(any());
    }

    @Test
    void canViewFinancials_flagTrue_true() {
        User u = user(User.Role.DEPT_HEAD);
        when(userPermissionRepository.findByUserId(u.getId()))
                .thenReturn(Optional.of(UserPermission.builder().user(u).viewFinancials(true).build()));
        assertThat(evaluator.canViewFinancials(u)).isTrue();
    }

    @Test
    void canViewFinancials_flagFalse_false() {
        User u = user(User.Role.DEPT_HEAD);
        when(userPermissionRepository.findByUserId(u.getId()))
                .thenReturn(Optional.of(UserPermission.builder().user(u).viewFinancials(false).build()));
        assertThat(evaluator.canViewFinancials(u)).isFalse();
    }

    @Test
    void canViewFinancials_flagNull_false() {
        User u = user(User.Role.DEPT_HEAD);
        UserPermission perm = UserPermission.builder().user(u).build();
        perm.setViewFinancials(null);
        when(userPermissionRepository.findByUserId(u.getId())).thenReturn(Optional.of(perm));
        assertThat(evaluator.canViewFinancials(u)).isFalse();
    }

    @Test
    void canViewFinancials_noRow_false() {
        User u = user(User.Role.VIEWER);
        when(userPermissionRepository.findByUserId(u.getId())).thenReturn(Optional.empty());
        assertThat(evaluator.canViewFinancials(u)).isFalse();
    }

    @Test
    void canViewFinancials_nullUser_false() {
        assertThat(evaluator.canViewFinancials(null)).isFalse();
    }

    @Test
    void canViewFinancials_isIndependentOfViewFinanceCategoryFlag() {
        User u = user(User.Role.DEPT_HEAD);
        when(userPermissionRepository.findByUserId(u.getId()))
                .thenReturn(Optional.of(UserPermission.builder().user(u)
                        .viewFinance(true).viewFinancials(false).build()));
        assertThat(evaluator.canViewFinancials(u)).isFalse();
    }
}
