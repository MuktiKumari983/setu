package com.sih.innovationplatform.impact;

import com.sih.innovationplatform.common.ApiException;
import com.sih.innovationplatform.project.ProjectAccessService;
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

@ExtendWith(MockitoExtension.class)
class ImpactAuthorizationTest {

    @Mock private ProjectAccessService projectAccessService;
    @Mock private ImpactRecordRepository recordRepo;
    @InjectMocks private ImpactService service;

    @Test
    void nonMemberCannotSubmitImpact() {
        doThrow(ApiException.forbidden("not a member"))
                .when(projectAccessService).requireMemberOrAdmin(1L, 2L);

        ImpactDtos.SubmitRequest request =
                new ImpactDtos.SubmitRequest(1L, 50, 0.0, "summary", 10, 5, "");
        ApiException exception = assertThrows(ApiException.class,
                () -> service.submit(1L, request, 2L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(recordRepo, never()).save(any());
    }

    @Test
    void nonMemberCannotAddEvidence() {
        doThrow(ApiException.forbidden("not a member"))
                .when(projectAccessService).requireMemberOrAdmin(1L, 2L);

        ImpactDtos.EvidenceRequest request =
                new ImpactDtos.EvidenceRequest(3L, "PHOTO", "description", "");
        assertThrows(ApiException.class, () -> service.addEvidence(1L, request, 2L));

        verify(projectAccessService).requireMemberOrAdmin(1L, 2L);
    }
}
