package com.sih.innovationplatform.impact;

import com.sih.innovationplatform.common.ApiException;
import com.sih.innovationplatform.common.CurrentUser;
import com.sih.innovationplatform.common.Roles;
import com.sih.innovationplatform.project.ProjectAccessService;
import com.sih.innovationplatform.user.User;
import com.sih.innovationplatform.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImpactReviewAuthorizationTest {

    @Mock private ImpactRecordRepository recordRepo;
    @Mock private ImpactEvidenceRepository evidenceRepo;
    @Mock private UserRepository userRepo;
    @Mock private CurrentUser currentUser;
    @Mock private ProjectAccessService projectAccessService;
    @InjectMocks private ImpactService service;

    @Test
    void nonAdminCannotReviewEvidence() {
        when(currentUser.hasAnyRole(Roles.PLATFORM_ADMINS)).thenReturn(false);

        ImpactDtos.ReviewRequest request = new ImpactDtos.ReviewRequest("VERIFIED");
        ApiException exception = assertThrows(ApiException.class,
                () -> service.reviewEvidence(3L, request, 2L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(evidenceRepo, never()).findById(any());
    }

    @Test
    void invalidReviewStatusIsRejected() {
        ImpactRecord record = new ImpactRecord();
        record.setId(5L);
        ImpactEvidence evidence = new ImpactEvidence();
        evidence.setId(3L);
        evidence.setImpactRecord(record);

        when(currentUser.hasAnyRole(Roles.PLATFORM_ADMINS)).thenReturn(true);
        when(evidenceRepo.findById(3L)).thenReturn(Optional.of(evidence));

        ImpactDtos.ReviewRequest request = new ImpactDtos.ReviewRequest("APPROVED");
        ApiException exception = assertThrows(ApiException.class,
                () -> service.reviewEvidence(3L, request, 2L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(evidenceRepo, never()).save(any());
    }

    @Test
    void verifiedReviewUpdatesEvidenceAndRecord() {
        ImpactRecord record = new ImpactRecord();
        record.setId(5L);
        record.setVerificationStatus(ImpactRecord.PENDING);
        ImpactEvidence evidence = new ImpactEvidence();
        evidence.setId(3L);
        evidence.setImpactRecord(record);
        evidence.setStatus("PENDING");
        User reviewer = new User();
        reviewer.setId(2L);
        reviewer.setName("Reviewer");

        when(currentUser.hasAnyRole(Roles.PLATFORM_ADMINS)).thenReturn(true);
        when(evidenceRepo.findById(3L)).thenReturn(Optional.of(evidence));
        when(userRepo.findById(2L)).thenReturn(Optional.of(reviewer));
        when(evidenceRepo.findByImpactRecordIdOrderByCreatedAtAsc(5L)).thenReturn(List.of(evidence));

        ImpactDtos.ReviewRequest request = new ImpactDtos.ReviewRequest("VERIFIED");
        ImpactDtos.ImpactResult result = service.reviewEvidence(3L, request, 2L);

        assertEquals(ImpactRecord.VERIFIED, result.verificationStatus());
        assertEquals("VERIFIED", evidence.getStatus());
        verify(evidenceRepo).save(evidence);
        verify(recordRepo).save(record);
    }
}
