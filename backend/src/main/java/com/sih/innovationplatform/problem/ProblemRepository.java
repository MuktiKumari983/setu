package com.sih.innovationplatform.problem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findByStatusNotOrderByCreatedAtDesc(String status);

    List<Problem> findByIdNot(Long id);

    long countByStatus(String status);
}