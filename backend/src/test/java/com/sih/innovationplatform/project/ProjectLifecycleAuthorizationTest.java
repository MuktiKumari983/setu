package com.sih.innovationplatform.project;

import com.sih.innovationplatform.common.ApiException;
import com.sih.innovationplatform.match.MatchingService;
import com.sih.innovationplatform.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ProjectLifecycleAuthorizationTest {

    @Mock private ProjectRepository projectRepo;
    @Mock private UserRepository userRepo;
    @Mock private FailureRecordRepository failureRepo;
    @Mock private MatchingService matchingService;
    @Mock private ProjectAccessService projectAccessService;
    @InjectMocks private ProjectService service;

    @Test
    void getForUserRequiresMembership() {
        doThrow(ApiException.forbidden("not a member"))
                .when(projectAccessService).requireMemberOrAdmin(1L, 2L);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.getForUser(1L, 2L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(projectRepo, never()).findById(1L);
    }

    @Test
    void changeStatusRequiresMembership() {
        doThrow(ApiException.forbidden("not a member"))
                .when(projectAccessService).requireMemberOrAdmin(1L, 2L);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.changeStatus(1L, new StatusRequest("PROPOSAL", "note"), 2L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(projectRepo, never()).save(any());
    }

    @Test
    void reportFailureRequiresMembership() {
        doThrow(ApiException.forbidden("not a member"))
                .when(projectAccessService).requireMemberOrAdmin(1L, 2L);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.reportFailure(1L, new FailureRequest("PILOT", "reason", "GIS"), 2L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(failureRepo, never()).save(any());
        verify(projectRepo, never()).save(any());
    }

    @Test
    void resolveFailureRequiresMembership() {
        doThrow(ApiException.forbidden("not a member"))
                .when(projectAccessService).requireMemberOrAdmin(1L, 2L);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.resolveFailure(1L, "note", 2L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(failureRepo, never()).save(any());
        verify(projectRepo, never()).save(any());
    }

    @Test
    void recoveryRecommendationsRequireMembership() {
        doThrow(ApiException.forbidden("not a member"))
                .when(projectAccessService).requireMemberOrAdmin(1L, 2L);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.recoveryRecommendations(1L, 2L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verifyNoInteractions(matchingService);
    }
}
