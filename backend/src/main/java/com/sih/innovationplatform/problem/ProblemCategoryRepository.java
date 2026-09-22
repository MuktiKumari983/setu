package com.sih.innovationplatform.problem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemCategoryRepository extends JpaRepository<ProblemCategory, Long> {

    java.util.Optional<ProblemCategory> findByName(String name);

    List<ProblemCategory> findAllByOrderByNameAsc();
}