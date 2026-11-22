package com.sih.innovationplatform.match;

import com.sih.innovationplatform.ai.AiGateway;
import com.sih.innovationplatform.common.ApiException;
import com.sih.innovationplatform.common.CurrentUser;
import com.sih.innovationplatform.common.Roles;
import com.sih.innovationplatform.problem.Problem;
import com.sih.innovationplatform.problem.ProblemRepository;
import com.sih.innovationplatform.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingAuthorizationTest {

    @Mock private ProblemRepository problemRepo;
    @Mock private AiGateway aiGateway;
    @Mock private CurrentUser currentUser;
    @InjectMocks private MatchingService service;

    private Problem problem;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setId(1L);
        problem = new Problem();
        problem.setId(10L);
        problem.setTitle("Water issue");
        problem.setDescription("Contaminated river");
        problem.setSubmitter(owner);
    }

    @Test
    void nonOwnerCannotRunMatching() {
        when(problemRepo.findById(10L)).thenReturn(Optional.of(problem));
        when(currentUser.hasAnyRole(Roles.PROBLEM_ADMINS)).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, () -> service.runMatching(10L, 2L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verifyNoInteractions(aiGateway);
    }

    @Test
    void nonOwnerCannotRunRecommendations() {
        when(problemRepo.findById(10L)).thenReturn(Optional.of(problem));
        when(currentUser.hasAnyRole(Roles.PROBLEM_ADMINS)).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, () -> service.recommend(10L, 2L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verifyNoInteractions(aiGateway);
    }
}
