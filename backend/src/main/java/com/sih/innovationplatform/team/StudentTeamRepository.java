package com.sih.innovationplatform.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentTeamRepository extends JpaRepository<StudentTeam, Long> {
    List<StudentTeam> findAllByOrderByNameAsc();
}