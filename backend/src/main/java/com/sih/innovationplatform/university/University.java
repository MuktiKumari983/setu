package com.sih.innovationplatform.university;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "universities")
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "description", length = 2000)
    private String description;

    public University(String name, String location, String description) {
        this.name = name;
        this.location = location;
        this.description = description;
    }
}