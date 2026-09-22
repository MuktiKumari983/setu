package com.sih.innovationplatform.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectCommentRepository extends JpaRepository<ProjectComment, Long> {
    List<ProjectComment> findByProjectIdOrderByCreatedAtAsc(Long projectId);
}