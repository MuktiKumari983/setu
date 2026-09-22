package com.sih.innovationplatform.impact;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImpactEvidenceRepository extends JpaRepository<ImpactEvidence, Long> {

    List<ImpactEvidence> findByImpactRecordIdOrderByCreatedAtAsc(Long impactRecordId);

    List<ImpactEvidence> findAllByOrderByCreatedAtDesc();
}