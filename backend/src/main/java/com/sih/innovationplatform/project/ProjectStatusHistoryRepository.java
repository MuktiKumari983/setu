package com.sih.innovationplatform.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectStatusHistoryRepository extends JpaRepository<ProjectStatusHistory, Long> {
    List<ProjectStatusHistory> findByProjectIdOrderByChangedAtAsc(Long projectId);
}