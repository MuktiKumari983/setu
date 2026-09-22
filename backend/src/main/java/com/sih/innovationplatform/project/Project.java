package com.sih.innovationplatform.project;

import com.sih.innovationplatform.industry.Industry;
import com.sih.innovationplatform.problem.Problem;
import com.sih.innovationplatform.university.Faculty;
import com.sih.innovationplatform.university.University;
import com.sih.innovationplatform.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "projects")
public class Project {

    public static final java.util.List<String> STAGES = java.util.List.of(
            "SUBMITTED", "VERIFIED", "MATCHED", "TEAM_FORMED", "PROPOSAL",
            "PROTOTYPE", "REVIEW", "PILOT", "IMPLEMENTATION", "IMPACT");

    public static final String NEEDS_SUPPORT = "NEEDS_SUPPORT";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "title", length = 250, nullable = false)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "university_id")
    private University university;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id")
    private com.sih.innovationplatform.team.StudentTeam team;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lead_mentor_id")
    private Faculty leadMentor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "industry_id")
    private Industry industry;

    @Column(name = "status", length = 30)
    private String status = "SUBMITTED";

    @Column(name = "progress_percent")
    private Integer progressPercent = 0;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}