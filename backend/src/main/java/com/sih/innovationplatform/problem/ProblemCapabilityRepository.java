package com.sih.innovationplatform.problem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemCapabilityRepository extends JpaRepository<ProblemCapability, Long> {
    List<ProblemCapability> findByProblemId(Long problemId);
}