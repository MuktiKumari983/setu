package com.sih.innovationplatform.problem;

import com.sih.innovationplatform.ai.AiGateway;
import com.sih.innovationplatform.ai.AiModels;
import com.sih.innovationplatform.common.ApiException;
import com.sih.innovationplatform.common.CurrentUser;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProblemServiceAuthorizationTest {

    @Mock private ProblemRepository problemRepo;
    @Mock private ProblemCategoryRepository categoryRepo;
    @Mock private ProblemCapabilityRepository capabilityRepo;
    @Mock private UserRepository userRepo;
    @Mock private AiGateway aiGateway;
    @Mock private CurrentUser currentUser;

    private ProblemService service;

    @BeforeEach
    void setUp() {
        service = new ProblemService(problemRepo, categoryRepo, capabilityRepo, userRepo, aiGateway, currentUser);
    }

    @Test
    void nonOwnerCannotReanalyzeProblem() {
        User owner = new User();
        owner.setId(1L);
        Problem problem = new Problem();
        problem.setId(10L);
        problem.setTitle("Water issue");
        problem.setDescription("River is contaminated");
        problem.setSubmitter(owner);

        when(problemRepo.findById(10L)).thenReturn(Optional.of(problem));
        when(currentUser.hasAnyRole("PLATFORM_ADMIN", "GOVERNMENT_ADMIN", "UNIVERSITY_ADMIN")).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, () -> service.runAnalysis(10L, 2L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verifyNoInteractions(aiGateway);
    }

    @Test
    void ownerCanReanalyzeProblem() {
        User owner = new User();
        owner.setId(1L);
        Problem problem = new Problem();
        problem.setId(10L);
        problem.setTitle("Water issue");
        problem.setDescription("River is contaminated");
        problem.setSubmitter(owner);

        when(problemRepo.findById(10L)).thenReturn(Optional.of(problem));
        when(problemRepo.findByIdNot(10L)).thenReturn(List.of());
        when(problemRepo.save(any(Problem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(aiGateway.analyze("Water issue", "River is contaminated", null))
                .thenReturn(new AiModels.AnalyzeResult("Water Quality", "HIGH", List.of(), List.of(), 0.7, "matched"));
        when(aiGateway.decompose(anyString(), anyString(), anyList())).thenReturn(List.of());

        service.runAnalysis(10L, 1L);

        verify(aiGateway).analyze("Water issue", "River is contaminated", null);
        verify(aiGateway).decompose(anyString(), anyString(), anyList());
    }
}
