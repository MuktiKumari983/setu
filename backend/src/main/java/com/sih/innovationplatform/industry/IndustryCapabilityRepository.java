package com.sih.innovationplatform.industry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndustryCapabilityRepository extends JpaRepository<IndustryCapability, Long> {
    List<IndustryCapability> findByIndustryId(Long industryId);
}
