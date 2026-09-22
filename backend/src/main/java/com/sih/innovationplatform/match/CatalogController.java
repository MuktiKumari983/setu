package com.sih.innovationplatform.match;

import com.sih.innovationplatform.industry.Industry;
import com.sih.innovationplatform.industry.IndustryRepository;
import com.sih.innovationplatform.team.StudentTeam;
import com.sih.innovationplatform.team.StudentTeamRepository;
import com.sih.innovationplatform.university.DepartmentRepository;
import com.sih.innovationplatform.university.Faculty;
import com.sih.innovationplatform.university.FacultyRepository;
import com.sih.innovationplatform.university.University;
import com.sih.innovationplatform.university.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CatalogController {

    private final UniversityRepository universityRepo;
    private final DepartmentRepository departmentRepo;
    private final FacultyRepository facultyRepo;
    private final StudentTeamRepository teamRepo;
    private final IndustryRepository industryRepo;

    @GetMapping("/api/universities")
    public List<Map<String, Object>> universities() {
        return universityRepo.findAll().stream().map(u -> Map.<String, Object>of(
                "id", u.getId(),
                "name", u.getName(),
                "location", safe(u.getLocation()),
                "description", safe(u.getDescription()),
                "departments", departmentRepo.findByUniversityId(u.getId()).stream()
                        .map(d -> d.getName()).toList(),
                "facultyCount", facultyRepo.findAll().stream()
                        .filter(f -> f.getUniversity() != null && f.getUniversity().getId().equals(u.getId())).count()))
                .toList();
    }

    @GetMapping("/api/student-teams")
    public List<Map<String, Object>> teams() {
        return teamRepo.findAll().stream().map(t -> Map.<String, Object>of(
                "id", t.getId(),
                "name", t.getName(),
                "university", t.getUniversity() == null ? "" : t.getUniversity().getName(),
                "description", safe(t.getDescription()),
                "skills", MatchingService.csv(t.getSkills()),
                "technologies", MatchingService.csv(t.getTechnologies()),
                "interests", MatchingService.csv(t.getInterests()),
                "availability", safe(t.getAvailability()),
                "previousProjects", safe(t.getPreviousProjects())))
                .toList();
    }

    @GetMapping("/api/faculty")
    public List<Map<String, Object>> faculty() {
        return facultyRepo.findAll().stream().map(f -> Map.<String, Object>of(
                "id", f.getId(),
                "name", f.getName(),
                "title", safe(f.getTitle()),
                "university", f.getUniversity() == null ? "" : f.getUniversity().getName(),
                "department", f.getDepartment() == null ? "" : f.getDepartment().getName(),
                "expertise", MatchingService.csv(f.getExpertise()),
                "researchArea", safe(f.getResearchArea()),
                "previousProjects", safe(f.getPreviousProjects())))
                .toList();
    }

    @GetMapping("/api/industries")
    public List<Map<String, Object>> industries() {
        return industryRepo.findAll().stream().map(i -> Map.<String, Object>ofEntries(
                Map.entry("id", i.getId()),
                Map.entry("name", i.getName()),
                Map.entry("sector", safe(i.getSector())),
                Map.entry("description", safe(i.getDescription())),
                Map.entry("expertise", MatchingService.csv(i.getExpertise())),
                Map.entry("technologies", MatchingService.csv(i.getTechnologies())),
                Map.entry("hardwareCapabilities", safe(i.getHardwareCapabilities())),
                Map.entry("fundingSupport", safe(i.getFundingSupport())),
                Map.entry("mentorship", safe(i.getMentorship())),
                Map.entry("testingCapability", safe(i.getTestingCapability())),
                Map.entry("deploymentCapability", safe(i.getDeploymentCapability())),
                Map.entry("location", safe(i.getLocation()))))
                .toList();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}