package com.sih.innovationplatform.dashboard;

import com.sih.innovationplatform.impact.ImpactService;
import com.sih.innovationplatform.industry.IndustryRepository;
import com.sih.innovationplatform.problem.Problem;
import com.sih.innovationplatform.problem.ProblemRepository;
import com.sih.innovationplatform.project.FailureRecordRepository;
import com.sih.innovationplatform.project.Project;
import com.sih.innovationplatform.project.ProjectRepository;
import com.sih.innovationplatform.university.UniversityRepository;
import com.sih.innovationplatform.match.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProblemRepository problemRepo;
    private final ProjectRepository projectRepo;
    private final MatchRepository matchRepo;
    private final FailureRecordRepository failureRepo;
    private final ImpactService impactService;
    private final UniversityRepository universityRepo;
    private final IndustryRepository industryRepo;

    public Map<String, Object> analytics() {
        List<Problem> problems = problemRepo.findAll();
        List<Project> projects = projectRepo.findAll();
        List<String> activeStages = Project.STAGES.stream().skip(2).toList();
        int activeProjects = (int) projects.stream()
                .filter(p -> activeStages.contains(p.getStatus())).count();
        int implemented = (int) projects.stream()
                .filter(p -> p.getStatus().equals("IMPACT") || p.getStatus().equals(Project.COMPLETED)).count();
        int needingSupport = (int) projects.stream()
                .filter(p -> p.getStatus().equals(Project.NEEDS_SUPPORT)).count();
        long failedPilots = failureRepo.count();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("totalChallenges", (long) problems.size());
        payload.put("submittedChallenges", problems.stream().filter(p -> "SUBMITTED".equals(p.getStatus())).count());
        payload.put("verifiedChallenges", problems.stream().filter(p ->
                List.of("VERIFIED", "MATCHED").contains(p.getStatus())).count());
        payload.put("matchedChallenges", problems.stream().filter(p -> "MATCHED".equals(p.getStatus())).count());
        payload.put("mergedChallenges", problems.stream().filter(p -> "MERGED".equals(p.getStatus())).count());
        payload.put("activeProjects", activeProjects);
        payload.put("implementedProjects", implemented);
        payload.put("failedPilots", failedPilots);
        payload.put("projectsNeedingSupport", needingSupport);
        payload.put("matchCount", projects.isEmpty() ? 0 : matchRepo.count());
        payload.put("verifiedBeneficiaries", impactService.verifiedBeneficiaries());
        payload.put("totalBeneficiaries", impactService.totalBeneficiaries());
        payload.put("universityCount", universityRepo.count());
        payload.put("industryCount", industryRepo.count());

        // Problems by category
        Map<String, Long> byCategory = new HashMap<>();
        for (Problem p : problems) {
            String name = p.getCategory() != null ? p.getCategory().getName() : "Uncategorized";
            byCategory.merge(name, 1L, Long::sum);
        }
        payload.put("byCategory", byCategory);

        // Problems by status (full lifecycle)
        Map<String, Long> byStatus = new HashMap<>();
        for (Problem p : problems) {
            byStatus.merge(p.getStatus(), 1L, Long::sum);
        }
        payload.put("byStatus", byStatus);

        // Projects by status timeline
        Map<String, Long> projectByStatus = new LinkedHashMap<>();
        for (String s : Project.STAGES) {
            projectByStatus.put(s, 0L);
        }
        projectByStatus.put(Project.NEEDS_SUPPORT, 0L);
        projectByStatus.put(Project.COMPLETED, 0L);
        for (Project p : projects) {
            projectByStatus.merge(p.getStatus(), 1L, Long::sum);
        }
        payload.put("projectsByStatus", projectByStatus);

        // University participation
        Map<String, Long> universityParticipation = new HashMap<>();
        for (Project p : projects) {
            if (p.getUniversity() != null) {
                universityParticipation.merge(p.getUniversity().getName(), 1L, Long::sum);
            }
        }
        payload.put("universityParticipation", universityParticipation);

        // Industry participation
        Map<String, Long> industryParticipation = new HashMap<>();
        for (Project p : projects) {
            if (p.getIndustry() != null) {
                industryParticipation.merge(p.getIndustry().getName(), 1L, Long::sum);
            }
        }
        payload.put("industryParticipation", industryParticipation);

        // Implementation rate (implemented / projects that reached pilot+ )
        long reachedImplementation = projects.stream().filter(p -> {
            int idx = Project.STAGES.indexOf(p.getStatus());
            return idx >= Project.STAGES.indexOf("IMPLEMENTATION") || p.getStatus().equals(Project.COMPLETED);
        }).count();
        payload.put("implementationRate", projects.isEmpty() ? 0.0
                : Math.round(100.0 * reachedImplementation / projects.size()) / 100.0);

        // Impact summary (only verified)
        payload.put("impactSummary", Map.of(
                "verifiedBeneficiaries", impactService.verifiedBeneficiaries(),
                "verifiedRecords", impactService.verifiedRecordCount(),
                "recordCount", impactService.recordCount()));

        return payload;
    }
}