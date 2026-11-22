package com.sih.innovationplatform.config;

import com.sih.innovationplatform.match.Match;
import com.sih.innovationplatform.match.MatchRepository;
import com.sih.innovationplatform.problem.Problem;
import com.sih.innovationplatform.problem.ProblemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSeederMatchIdempotencyTest {

    private static final long PROBLEM_ID = 1277L;
    private static final long UNIVERSITY_ID = 640L;
    private static final List<String> FACTORS = List.of("Matched skills", "Research alignment");

    @Mock private ProblemRepository problemRepo;
    @Mock private MatchRepository matchRepo;
    @InjectMocks private DataSeeder seeder;

    @Test
    void seedingTheSameMatchTwicePersistsOnlyOneMatch() {
        when(matchRepo.findByProblemIdAndEntityTypeAndEntityId(PROBLEM_ID, "UNIVERSITY", UNIVERSITY_ID))
                .thenReturn(Optional.empty());
        when(problemRepo.getReferenceById(PROBLEM_ID)).thenReturn(problem());

        seeder.match(PROBLEM_ID, "UNIVERSITY", UNIVERSITY_ID, 0.90, "Listed departments", FACTORS);
        seeder.match(PROBLEM_ID, "UNIVERSITY", UNIVERSITY_ID, 0.90, "Listed departments", FACTORS);

        verify(matchRepo, times(1))
                .findByProblemIdAndEntityTypeAndEntityId(PROBLEM_ID, "UNIVERSITY", UNIVERSITY_ID);
        verify(matchRepo, times(1)).save(any(Match.class));
    }

    @Test
    void duplicateCandidatesWithinOneSeedRunKeepTheFirstCandidate() {
        when(matchRepo.findByProblemIdAndEntityTypeAndEntityId(PROBLEM_ID, "UNIVERSITY", UNIVERSITY_ID))
                .thenReturn(Optional.empty());
        when(problemRepo.getReferenceById(PROBLEM_ID)).thenReturn(problem());

        seeder.match(PROBLEM_ID, "UNIVERSITY", UNIVERSITY_ID, 0.90, "first", List.of("first"));
        seeder.match(PROBLEM_ID, "UNIVERSITY", UNIVERSITY_ID, 0.10, "second", List.of("second"));

        ArgumentCaptor<Match> saved = ArgumentCaptor.forClass(Match.class);
        verify(matchRepo).save(saved.capture());
        assertEquals(0.90, saved.getValue().getScore(), 0.000001);
        assertEquals("first", saved.getValue().getExplanation());
        assertEquals("first", saved.getValue().getContributingFactors());
    }

    @Test
    void restartingSeedingReusesAlreadyPersistedMatchWithoutMutatingIt() {
        Match existing = persistedMatch();
        existing.setScore(0.42);
        existing.setExplanation("previously persisted");
        existing.setStatus("ACCEPTED");
        when(matchRepo.findByProblemIdAndEntityTypeAndEntityId(PROBLEM_ID, "UNIVERSITY", UNIVERSITY_ID))
                .thenReturn(Optional.of(existing));

        seeder.match(PROBLEM_ID, "UNIVERSITY", UNIVERSITY_ID, 0.99, "seed explanation", FACTORS);

        verify(matchRepo, never()).save(any(Match.class));
        verifyNoInteractions(problemRepo);
        assertEquals(0.42, existing.getScore(), 0.000001);
        assertEquals("previously persisted", existing.getExplanation());
        assertEquals("ACCEPTED", existing.getStatus());
    }

    @Test
    void newMatchPreservesProblemAndEntityRelationship() {
        Problem problem = problem();
        when(matchRepo.findByProblemIdAndEntityTypeAndEntityId(PROBLEM_ID, "UNIVERSITY", UNIVERSITY_ID))
                .thenReturn(Optional.empty());
        when(problemRepo.getReferenceById(PROBLEM_ID)).thenReturn(problem);

        seeder.match(PROBLEM_ID, "UNIVERSITY", UNIVERSITY_ID, 0.94, "Listed departments", FACTORS);

        ArgumentCaptor<Match> saved = ArgumentCaptor.forClass(Match.class);
        verify(matchRepo).save(saved.capture());
        assertSame(problem, saved.getValue().getProblem());
        assertEquals(PROBLEM_ID, saved.getValue().getProblem().getId());
        assertEquals("UNIVERSITY", saved.getValue().getEntityType());
        assertEquals(UNIVERSITY_ID, saved.getValue().getEntityId());
        assertEquals(0.94, saved.getValue().getScore(), 0.000001);
        assertEquals("PROPOSED", saved.getValue().getStatus());
    }

    private static Problem problem() {
        Problem problem = new Problem();
        problem.setId(PROBLEM_ID);
        return problem;
    }

    private static Match persistedMatch() {
        Match match = new Match();
        match.setProblem(problem());
        match.setEntityType("UNIVERSITY");
        match.setEntityId(UNIVERSITY_ID);
        return match;
    }
}
