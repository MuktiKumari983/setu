package com.sih.innovationplatform.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailureRecordRepository extends JpaRepository<FailureRecord, Long> {
    List<FailureRecord> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}