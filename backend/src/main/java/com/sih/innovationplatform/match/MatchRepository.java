package com.sih.innovationplatform.match;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByProblemIdOrderByScoreDesc(Long problemId);

    List<Match> findByProblemIdAndEntityTypeOrderByScoreDesc(Long problemId, String entityType);

    Optional<Match> findByProblemIdAndEntityTypeAndEntityId(Long problemId, String entityType, Long entityId);

    long countByProblemId(Long problemId);
}