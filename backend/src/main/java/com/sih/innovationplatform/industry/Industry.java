package com.sih.innovationplatform.industry;

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
@Table(name = "industries")
public class Industry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "sector", length = 150)
    private String sector;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "expertise", length = 1000)
    private String expertise;

    @Column(name = "technologies", length = 1000)
    private String technologies;

    @Column(name = "hardware_capabilities", length = 1000)
    private String hardwareCapabilities;

    @Column(name = "funding_support", length = 500)
    private String fundingSupport;

    @Column(name = "mentorship", length = 500)
    private String mentorship;

    @Column(name = "testing_capability", length = 500)
    private String testingCapability;

    @Column(name = "deployment_capability", length = 500)
    private String deploymentCapability;

    @Column(name = "location", length = 200)
    private String location;

    public Industry(String name, String sector, String description, String expertise, String technologies,
                    String hardwareCapabilities, String fundingSupport, String mentorship,
                    String testingCapability, String deploymentCapability, String location) {
        this.name = name;
        this.sector = sector;
        this.description = description;
        this.expertise = expertise;
        this.technologies = technologies;
        this.hardwareCapabilities = hardwareCapabilities;
        this.fundingSupport = fundingSupport;
        this.mentorship = mentorship;
        this.testingCapability = testingCapability;
        this.deploymentCapability = deploymentCapability;
        this.location = location;
    }
}