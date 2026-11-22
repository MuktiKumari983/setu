package com.sih.innovationplatform.problem;

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
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "problems")
public class Problem {

    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_VERIFIED = "VERIFIED";
    public static final String STATUS_MATCHED = "MATCHED";
    public static final String STATUS_MERGED = "MERGED";
    public static final String STATUS_REJECTED = "REJECTED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submitter_id")
    private User submitter;

    @Column(name = "title", length = 250, nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private ProblemCategory category;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "estimated_beneficiaries")
    private Integer estimatedBeneficiaries = 0;

    @Column(name = "urgency", length = 20)
    private String urgency = "MEDIUM";

    @Column(name = "required_outcome", length = 1000)
    private String requiredOutcome;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "status", length = 30)
    private String status = STATUS_SUBMITTED;

    @Column(name = "ai_category", length = 150)
    private String aiCategory;

    @Column(name = "ai_urgency", length = 20)
    private String aiUrgency;

    @Column(name = "ai_confidence")
    private Double aiConfidence;

    @Column(name = "ai_skills", length = 1000)
    private String aiSkills;

    @Column(name = "ai_keywords", length = 1000)
    private String aiKeywords;

    @Column(name = "ai_explanation", columnDefinition = "TEXT")
    private String aiExplanation;

    @Column(name = "ai_root_causes", columnDefinition = "TEXT")
    private String aiRootCauses;

    @Column(name = "ai_target_beneficiaries", columnDefinition = "TEXT")
    private String aiTargetBeneficiaries;

    @Column(name = "ai_challenges", columnDefinition = "TEXT")
    private String aiChallenges;

    @Column(name = "ai_solution_directions", columnDefinition = "TEXT")
    private String aiSolutionDirections;

    @Column(name = "ai_expected_outcomes", columnDefinition = "TEXT")
    private String aiExpectedOutcomes;

    @Column(name = "ai_source", length = 20)
    private String aiSource;

    @Column(name = "ai_similarity")
    private Double aiSimilarity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "similar_problem_id")
    private Problem similarProblem;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}