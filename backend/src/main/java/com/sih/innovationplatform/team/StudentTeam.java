package com.sih.innovationplatform.team;

import com.sih.innovationplatform.university.University;
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

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "student_teams")
public class StudentTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @Column(name = "lead_id")
    private Long leadId;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "description", length = 1500)
    private String description;

    @Column(name = "skills", length = 1000)
    private String skills;

    @Column(name = "technologies", length = 1000)
    private String technologies;

    @Column(name = "interests", length = 1000)
    private String interests;

    @Column(name = "availability", length = 100)
    private String availability;

    @Column(name = "previous_projects", length = 1500)
    private String previousProjects;

    @Column(name = "status", length = 30)
    private String status = "ACTIVE";

    public StudentTeam(University university, String name, String description, String skills,
                       String technologies, String interests, String availability, String previousProjects) {
        this.university = university;
        this.name = name;
        this.description = description;
        this.skills = skills;
        this.technologies = technologies;
        this.interests = interests;
        this.availability = availability;
        this.previousProjects = previousProjects;
    }
}