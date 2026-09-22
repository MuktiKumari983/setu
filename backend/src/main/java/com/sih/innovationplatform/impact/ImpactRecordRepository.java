package com.sih.innovationplatform.impact;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImpactRecordRepository extends JpaRepository<ImpactRecord, Long> {

    List<ImpactRecord> findByProjectIdOrderBySubmittedAtDesc(Long projectId);

    long countByVerificationStatus(String verificationStatus);

    List<ImpactRecord> findByVerificationStatus(String verificationStatus);
}