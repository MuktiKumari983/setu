package com.sih.innovationplatform.project;

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
@Table(name = "failure_records")
public class FailureRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "stage", length = 30)
    private String stage;

    @Column(name = "reason", length = 2000)
    private String reason;

    @Column(name = "missing_capability", length = 300)
    private String missingCapability;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reported_by")
    private User reportedBy;

    @Column(name = "resolved")
    private Boolean resolved = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}