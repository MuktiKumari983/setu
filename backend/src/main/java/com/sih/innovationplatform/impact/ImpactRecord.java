package com.sih.innovationplatform.impact;

import com.sih.innovationplatform.project.Project;
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
@Table(name = "impact_records")
public class ImpactRecord {

    public static final String PENDING = "PENDING";
    public static final String PARTIALLY_VERIFIED = "PARTIALLY_VERIFIED";
    public static final String VERIFIED = "VERIFIED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "beneficiaries")
    private Long beneficiaries = 0L;

    @Column(name = "locations_covered")
    private Integer locationsCovered = 0;

    @Column(name = "cost_savings")
    private Double costSavings = 0.0;

    @Column(name = "problem_reduction", length = 1000)
    private String problemReduction;

    @Column(name = "duration_months")
    private Integer durationMonths = 0;

    @Column(name = "implementation_percent")
    private Integer implementationPercent = 0;

    @Column(name = "domain_metrics", length = 1500)
    private String domainMetrics;

    @Column(name = "verification_status", length = 30)
    private String verificationStatus = PENDING;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submitted_by")
    private User submittedBy;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}