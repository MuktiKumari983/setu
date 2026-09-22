package com.sih.innovationplatform.industry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndustryRepository extends JpaRepository<Industry, Long> {
    List<Industry> findAllByOrderByNameAsc();
}