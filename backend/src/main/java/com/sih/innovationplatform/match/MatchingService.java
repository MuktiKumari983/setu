package com.sih.innovationplatform.match;

import com.sih.innovationplatform.ai.AiGateway;
import com.sih.innovationplatform.ai.AiModels;
import com.sih.innovationplatform.ai.MatchType;
import com.sih.innovationplatform.config.AppProperties;
import com.sih.innovationplatform.industry.Industry;
import com.sih.innovationplatform.industry.IndustryRepository;
import com.sih.innovationplatform.problem.Problem;
import com.sih.innovationplatform.problem.ProblemCapability;
import com.sih.innovationplatform.problem.ProblemCapabilityRepository;
import com.sih.innovationplatform.problem.ProblemRepository;
import com.sih.innovationplatform.team.StudentTeam;
import com.sih.innovationplatform.team.StudentTeamRepository;
import com.sih.innovationplatform.university.DepartmentRepository;
import com.sih.innovationplatform.university.Faculty;
import com.sih.innovationplatform.university.FacultyRepository;
import com.sih.innovationplatform.university.University;
import com.sih.innovationplatform.university.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final UniversityRepository universityRepo;
    private final DepartmentRepository departmentRepo;
    private final FacultyRepository facultyRepo;
    private final StudentTeamRepository teamRepo;
    private final IndustryRepository industryRepo;
    private final ProblemRepository problemRepo;
    private final ProblemCapabilityRepository capabilityRepo;
    private final MatchRepository matchRepo;
    private final AiGateway aiGateway;
    private final AppProperties appProperties;

    @Transactional
    public List<MatchDto> runMatching(Long problemId) {
        Problem problem = problemRepo.findById(problemId)
                .orElseThrow(() -> com.sih.innovationplatform.common.ApiException.notFound("Problem not found"));
        AiModels.MatchingRequirement requirement = requirement(problem);

        List<MatchDto> result = new ArrayList<>();
        for (MatchType type : MatchType.values()) {
            List<AiModels.ScoredMatch> scores = aiGateway.match(type, requirement, candidates(type), appProperties);
            for (AiModels.ScoredMatch s : scores) {
                upsert(problemId, type.name(), s);
                result.add(new MatchDto(type.name(), s.id(), resolveName(type.name(), s.id()), s.score(),
                        s.explanation(), s.contributingFactors(), "PROPOSED"));
            }
        }
        if (Problem.STATUS_VERIFIED.equals(problem.getStatus())) {
            problem.setStatus(Problem.STATUS_MATCHED);
            problemRepo.save(problem);
        }
        return result;
    }

    public List<MatchDto> matchesFor(Long problemId, String type) {
        List<Match> rows = (type == null || type.isBlank())
                ? matchRepo.findByProblemIdOrderByScoreDesc(problemId)
                : matchRepo.findByProblemIdAndEntityTypeOrderByScoreDesc(problemId, type.toUpperCase());
        return rows.stream().map(this::toDto).toList();
    }

    @Transactional
    public AiModels.Recommendation recommend(Long problemId) {
        Problem problem = problemRepo.findById(problemId)
                .orElseThrow(() -> com.sih.innovationplatform.common.ApiException.notFound("Problem not found"));
        AiModels.MatchingRequirement requirement = requirement(problem);
        AiModels.Recommendation rec = aiGateway.recommend(
                requirement,
                candidates(MatchType.UNIVERSITY),
                candidates(MatchType.TEAM),
                candidates(MatchType.MENTOR),
                candidates(MatchType.INDUSTRY),
                appProperties);
        for (AiModels.StakeholderPick pick : rec.stakeholders()) {
            String type = pick.type().equals("STUDENT_TEAM") ? "TEAM" : pick.type();
            upsert(problemId, type, pick.id(), Math.round(pick.score() * 100.0) / 100.0,
                    pick.reason(), List.of());
        }
        if (Problem.STATUS_VERIFIED.equals(problem.getStatus())) {
            problem.setStatus(Problem.STATUS_MATCHED);
            problemRepo.save(problem);
        }
        return rec;
    }

    public AiModels.RecoveryRecommendation recovery(Long problemId, String missingCapability) {
        Problem problem = problemRepo.findById(problemId)
                .orElseThrow(() -> com.sih.innovationplatform.common.ApiException.notFound("Problem not found"));
        AiModels.MatchingRequirement requirement = requirement(problem);
        return aiGateway.recover(
                missingCapability,
                requirement,
                candidates(MatchType.UNIVERSITY),
                candidates(MatchType.TEAM),
                candidates(MatchType.MENTOR),
                candidates(MatchType.INDUSTRY),
                appProperties);
    }

    public List<AiModels.CandidateProfile> candidates(MatchType type) {
        return switch (type) {
            case UNIVERSITY -> universityRepo.findAll().stream().map(this::universityProfile).toList();
            case TEAM -> teamRepo.findAll().stream().map(this::teamProfile).toList();
            case MENTOR -> facultyRepo.findAll().stream().map(this::mentorProfile).toList();
            case INDUSTRY -> industryRepo.findAll().stream().map(this::industryProfile).toList();
        };
    }

    private AiModels.CandidateProfile universityProfile(University u) {
        List<String> depts = departmentRepo.findByUniversityId(u.getId())
                .stream().map(d -> d.getName()).toList();
        List<String> tags = new ArrayList<>(depts);
        if (u.getDescription() != null) {
            tags.add(u.getDescription());
        }
        return new AiModels.CandidateProfile(u.getId(), u.getName(), u.getDescription(), depts, tags, depts);
    }

    private AiModels.CandidateProfile teamProfile(StudentTeam t) {
        List<String> skills = new ArrayList<>(csv(t.getSkills()));
        skills.addAll(csv(t.getTechnologies()));
        List<String> tags = new ArrayList<>(csv(t.getInterests()));
        tags.addAll(csv(t.getPreviousProjects()));
        tags.add(t.getName());
        return new AiModels.CandidateProfile(t.getId(), t.getName(),
                (t.getDescription() == null ? "" : t.getDescription()) + " " + t.getAvailability(),
                skills, tags, csv(t.getTechnologies()));
    }

    private AiModels.CandidateProfile mentorProfile(Faculty f) {
        List<String> skills = new ArrayList<>(csv(f.getExpertise()));
        List<String> tags = new ArrayList<>();
        if (f.getResearchArea() != null) tags.add(f.getResearchArea());
        tags.addAll(csv(f.getPreviousProjects()));
        if (f.getDepartment() != null) tags.add(f.getDepartment().getName());
        if (f.getTitle() != null) tags.add(f.getTitle());
        return new AiModels.CandidateProfile(f.getId(), f.getName(),
                f.getResearchArea(), skills, tags, skills);
    }

    private AiModels.CandidateProfile industryProfile(Industry i) {
        List<String> skills = new ArrayList<>(csv(i.getExpertise()));
        skills.addAll(csv(i.getTechnologies()));
        skills.addAll(csv(i.getHardwareCapabilities()));
        List<String> tags = new ArrayList<>(csv(i.getSector()));
        if (i.getDescription() != null) tags.add(i.getDescription());
        if (i.getFundingSupport() != null) tags.add(i.getFundingSupport());
        if (i.getMentorship() != null) tags.add(i.getMentorship());
        if (i.getTestingCapability() != null) tags.add(i.getTestingCapability());
        if (i.getDeploymentCapability() != null) tags.add(i.getDeploymentCapability());
        return new AiModels.CandidateProfile(i.getId(), i.getName(), i.getDescription(), skills, tags, csv(i.getExpertise()));
    }

    public AiModels.MatchingRequirement requirement(Problem p) {
        List<String> skills = csv(p.getAiSkills());
        List<String> keywords = csv(p.getAiKeywords());
        String category = p.getAiCategory() != null ? p.getAiCategory()
                : (p.getCategory() != null ? p.getCategory().getName() : "");
        List<String> capabilities = capabilityRepo.findByProblemId(p.getId())
                .stream().map(ProblemCapability::getCapability).toList();
        return new AiModels.MatchingRequirement(category, keywords, skills, capabilities);
    }

    private void upsert(Long problemId, String type, AiModels.ScoredMatch s) {
        upsert(problemId, type, s.id(), s.score(), s.explanation(), s.contributingFactors());
    }

    private void upsert(Long problemId, String type, Long entityId, double score, String explanation,
                        List<String> contributingFactors) {
        Problem problem = problemRepo.findById(problemId).orElse(null);
        Match match = matchRepo.findByProblemIdAndEntityTypeAndEntityId(problemId, type, entityId)
                .orElseGet(Match::new);
        match.setProblem(problem);
        match.setEntityType(type);
        match.setEntityId(entityId);
        match.setScore(score);
        match.setExplanation(explanation);
        match.setContributingFactors(String.join(" | ", contributingFactors));
        if (match.getId() == null) {
            match.setStatus("PROPOSED");
        }
        matchRepo.save(match);
    }

    private MatchDto toDto(Match m) {
        return new MatchDto(m.getEntityType(), m.getEntityId(), resolveName(m.getEntityType(), m.getEntityId()),
                m.getScore(), m.getExplanation(),
                Arrays.stream((m.getContributingFactors() == null ? "" : m.getContributingFactors()).split("\\|"))
                        .map(String::trim).filter(s -> !s.isBlank()).toList(),
                m.getStatus());
    }

    private String resolveName(String type, Long id) {
        try {
            return switch (type) {
                case "UNIVERSITY" -> universityRepo.findById(id).map(University::getName).orElse("—");
                case "TEAM" -> teamRepo.findById(id).map(StudentTeam::getName).orElse("—");
                case "MENTOR" -> facultyRepo.findById(id).map(Faculty::getName).orElse("—");
                case "INDUSTRY" -> industryRepo.findById(id).map(Industry::getName).orElse("—");
                default -> "—";
            };
        } catch (Exception e) {
            return "—";
        }
    }

    static List<String> csv(String s) {
        if (s == null || s.isBlank()) {
            return List.of();
        }
        return Arrays.stream(s.split(",")).map(String::trim).filter(x -> !x.isBlank()).toList();
    }
}