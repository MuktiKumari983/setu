package com.sih.innovationplatform.match;

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
import com.sih.innovationplatform.problem.Problem;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "entity_type", length = 30, nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "score")
    private Double score;

    @Column(name = "explanation", length = 2000)
    private String explanation;

    @Column(name = "contributing_factors", length = 1500)
    private String contributingFactors;

    @Column(name = "status", length = 30)
    private String status = "PROPOSED";

    @Column(name = "source", length = 20)
    private String source;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}