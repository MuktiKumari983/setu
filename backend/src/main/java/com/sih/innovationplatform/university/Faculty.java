package com.sih.innovationplatform.university;

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
@Table(name = "faculty")
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "title", length = 150)
    private String title;

    @Column(name = "expertise", length = 1000)
    private String expertise;

    @Column(name = "research_area", length = 500)
    private String researchArea;

    @Column(name = "previous_projects", length = 1500)
    private String previousProjects;

    @Column(name = "email", length = 150)
    private String email;

    public Faculty(University university, Department department, String name, String title,
                   String expertise, String researchArea, String previousProjects, String email) {
        this.university = university;
        this.department = department;
        this.name = name;
        this.title = title;
        this.expertise = expertise;
        this.researchArea = researchArea;
        this.previousProjects = previousProjects;
        this.email = email;
    }
}