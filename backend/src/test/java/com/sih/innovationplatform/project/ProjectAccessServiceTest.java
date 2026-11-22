package com.sih.innovationplatform.project;

import com.sih.innovationplatform.common.ApiException;
import com.sih.innovationplatform.common.CurrentUser;
import com.sih.innovationplatform.common.Roles;
import com.sih.innovationplatform.problem.Problem;
import com.sih.innovationplatform.user.User;
import com.sih.innovationplatform.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAccessServiceTest {

    @Mock private ProjectRepository projectRepo;
    @Mock private ProjectMemberRepository memberRepo;
    @Mock private UserRepository userRepo;
    @Mock private CurrentUser currentUser;

    private ProjectAccessService service;

    @BeforeEach
    void setUp() {
        service = new ProjectAccessService(projectRepo, memberRepo, userRepo, currentUser);
    }

    @Test
    void nonMemberCannotMutateProject() {
        User owner = new User();
        owner.setId(1L);
        User actor = new User();
        actor.setId(2L);
        Problem problem = new Problem();
        problem.setSubmitter(owner);
        Project project = new Project();
        project.setId(10L);
        project.setProblem(problem);

        when(currentUser.hasAnyRole(Roles.PLATFORM_ADMINS)).thenReturn(false);
        when(userRepo.findById(2L)).thenReturn(Optional.of(actor));
        when(projectRepo.findById(10L)).thenReturn(Optional.of(project));
        when(memberRepo.findByProjectId(10L)).thenReturn(List.of());

        ApiException exception = assertThrows(ApiException.class,
                () -> service.requireMemberOrAdmin(10L, 2L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void platformAdminBypassesMembershipCheck() {
        when(currentUser.hasAnyRole(Roles.PLATFORM_ADMINS)).thenReturn(true);

        assertDoesNotThrow(() -> service.requireMemberOrAdmin(10L, 2L));
        verify(userRepo, never()).findById(any());
    }
}
