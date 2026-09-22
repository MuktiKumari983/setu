package com.sih.innovationplatform.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOrderByCreatedAtDesc();

    long countByStatus(String status);

    long countByStatusIn(List<String> statuses);

    List<Project> findByStatusOrderByCreatedAtDesc(String status);
}