package com.sih.innovationplatform.problem;

import com.sih.innovationplatform.ai.AiGateway;
import com.sih.innovationplatform.ai.AiModels;
import com.sih.innovationplatform.common.ApiException;
import com.sih.innovationplatform.common.CurrentUser;
import com.sih.innovationplatform.common.Roles;
import com.sih.innovationplatform.user.User;
import com.sih.innovationplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepo;
    private final ProblemCategoryRepository categoryRepo;
    private final ProblemCapabilityRepository capabilityRepo;
    private final UserRepository userRepo;
    private final AiGateway aiGateway;
    private final CurrentUser currentUser;

    @Transactional
    public ProblemResponse create(ProblemCreateRequest req, Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        if (req.title() == null || req.title().isBlank()) {
            throw ApiException.badRequest("Title is required");
        }
        if (req.description() == null || req.description().isBlank()) {
            throw ApiException.badRequest("Description is required");
        }

        Problem problem = new Problem();
        problem.setTitle(req.title().trim());
        problem.setDescription(req.description().trim());
        problem.setLocation(req.location());
        problem.setEstimatedBeneficiaries(req.estimatedBeneficiaries() != null ? req.estimatedBeneficiaries() : 0);
        problem.setUrgency(req.urgency() != null ? req.urgency().toUpperCase() : "MEDIUM");
        problem.setRequiredOutcome(req.requiredOutcome());
        problem.setDocumentUrl(req.documentUrl());
        problem.setSubmitter(user);
        problem.setStatus(Problem.STATUS_SUBMITTED);

        Problem saved = problemRepo.save(problem);
        runAnalysis(saved.getId(), userId);
        return get(saved.getId());
    }

    @Transactional
    public void runAnalysis(Long problemId) {
        runAnalysis(problemId, currentUser.get().getId());
    }

    @Transactional
    public void runAnalysis(Long problemId, Long userId) {
        Problem problem = problemRepo.findById(problemId)
                .orElseThrow(() -> ApiException.notFound("Problem not found"));
        requireSubmitterOrAdmin(problem, userId);

        AiModels.AnalyzeResult analysis = aiGateway.analyze(
                problem.getTitle(), problem.getDescription(), problem.getRequiredOutcome());
        problem.setAiCategory(analysis.category());
        problem.setAiUrgency(analysis.urgency());
        problem.setAiSkills(String.join(", ", analysis.requiredSkills()));
        problem.setAiKeywords(String.join(", ", analysis.keywords()));
        problem.setAiConfidence(analysis.confidence());
        problem.setAiExplanation(analysis.explanation());
        problem.setAiRootCauses(String.join(", ", analysis.rootCauses()));
        problem.setAiTargetBeneficiaries(String.join(", ", analysis.targetBeneficiaries()));
        problem.setAiChallenges(String.join(", ", analysis.challenges()));
        problem.setAiSolutionDirections(String.join(", ", analysis.solutionDirections()));
        problem.setAiExpectedOutcomes(String.join(", ", analysis.expectedOutcomes()));
        problem.setAiSource(analysis.source().name());
        if (problem.getUrgency() == null || "MEDIUM".equals(problem.getUrgency())) {
            problem.setUrgency(analysis.urgency());
        }

        if (problem.getCategory() == null && analysis.category() != null && !analysis.category().isBlank()) {
            ProblemCategory cat = categoryRepo.findByName(analysis.category())
                    .orElseGet(() -> categoryRepo.save(new ProblemCategory(analysis.category(), "")));
            problem.setCategory(cat);
        }

        capabilityRepo.findByProblemId(problemId).forEach(cap -> capabilityRepo.delete(cap));
        capabilityRepo.flush();
        List<AiModels.Capability> capabilities = aiGateway.decompose(
                problem.getTitle(), problem.getDescription(), analysis.requiredSkills());
        for (AiModels.Capability cap : capabilities) {
            ProblemCapability pc = new ProblemCapability();
            pc.setProblem(problem);
            pc.setCapability(cap.capability());
            pc.setRequiredSkills(String.join(", ", cap.requiredSkills()));
            pc.setDescription(cap.description());
            pc.setPriority(cap.priority());
            capabilityRepo.save(pc);
        }

        List<AiModels.ExistingProblem> existing = problemRepo.findByIdNot(problemId).stream()
                .limit(100)
                .map(p -> new AiModels.ExistingProblem(p.getId(), p.getTitle(), p.getDescription()))
                .toList();
        if (!existing.isEmpty()) {
            List<AiModels.SimilarProblem> dupes = aiGateway.duplicates(
                    problem.getTitle(), problem.getDescription(), existing);
            if (!dupes.isEmpty()) {
                AiModels.SimilarProblem top = dupes.get(0);
                problem.setAiSimilarity(top.similarity());
                problem.setSimilarProblem(top.id() != null && top.id() > 0
                        ? problemRepo.findById(top.id()).orElse(null) : null);
            } else {
                problem.setAiSimilarity(null);
                problem.setSimilarProblem(null);
            }
        }

        problem.setUpdatedAt(LocalDateTime.now());
        problemRepo.save(problem);
    }

    public List<ProblemResponse> list(String status, String category, String q) {
        return problemRepo.findByStatusNotOrderByCreatedAtDesc("MERGED").stream()
                .filter(p -> status == null || status.isBlank() || status.equalsIgnoreCase(p.getStatus()))
                .filter(p -> category == null || category.isBlank()
                        || p.getCategory() != null && category.equalsIgnoreCase(p.getCategory().getName()))
                .filter(p -> q == null || q.isBlank()
                        || p.getTitle().toLowerCase().contains(q.toLowerCase())
                        || p.getDescription().toLowerCase().contains(q.toLowerCase()))
                .map(this::toResponse)
                .toList();
    }

    public ProblemResponse get(Long id) {
        Problem problem = problemRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Problem not found"));
        return toResponse(problem);
    }

    @Transactional
    public ProblemResponse update(Long id, ProblemUpdateRequest req, Long userId) {
        Problem p = problemRepo.findById(id).orElseThrow(() -> ApiException.notFound("Problem not found"));
        if (!p.getSubmitter().getId().equals(userId) && !currentUser.hasAnyRole(Roles.PLATFORM_ADMINS)) {
            throw ApiException.forbidden("Only the submitter or an admin can edit this problem");
        }
        if (req.title() != null) p.setTitle(req.title().trim());
        if (req.description() != null) p.setDescription(req.description().trim());
        if (req.location() != null) p.setLocation(req.location());
        if (req.estimatedBeneficiaries() != null) p.setEstimatedBeneficiaries(req.estimatedBeneficiaries());
        if (req.urgency() != null) p.setUrgency(req.urgency().toUpperCase());
        if (req.requiredOutcome() != null) p.setRequiredOutcome(req.requiredOutcome());
        if (req.documentUrl() != null) p.setDocumentUrl(req.documentUrl());
        p.setUpdatedAt(LocalDateTime.now());
        return toResponse(problemRepo.save(p));
    }

    @Transactional
    public void verify(Long id) {
        if (!currentUser.hasAnyRole(Roles.PROBLEM_ADMINS)) {
            throw ApiException.forbidden("Only admins can verify challenges");
        }
        Problem p = problemRepo.findById(id).orElseThrow(() -> ApiException.notFound("Problem not found"));
        if (Problem.STATUS_SUBMITTED.equals(p.getStatus()) || Problem.STATUS_VERIFIED.equals(p.getStatus())) {
            p.setStatus(Problem.STATUS_VERIFIED);
            p.setUpdatedAt(LocalDateTime.now());
            problemRepo.save(p);
        }
    }

    @Transactional
    public void merge(Long id, Long targetId) {
        if (!currentUser.hasAnyRole(Roles.PROBLEM_ADMINS)) {
            throw ApiException.forbidden("Only admins can merge challenges");
        }
        Problem p = problemRepo.findById(id).orElseThrow(() -> ApiException.notFound("Problem not found"));
        problemRepo.findById(targetId).orElseThrow(() -> ApiException.notFound("Target problem not found"));
        p.setStatus(Problem.STATUS_MERGED);
        p.setSimilarProblem(problemRepo.findById(targetId).orElse(null));
        p.setUpdatedAt(LocalDateTime.now());
        problemRepo.save(p);
    }

    public List<ProblemResponse> duplicatesFor(Long id) {
        Problem p = problemRepo.findById(id).orElseThrow(() -> ApiException.notFound("Problem not found"));
        List<AiModels.ExistingProblem> existing = problemRepo.findByIdNot(id).stream()
                .limit(100)
                .map(x -> new AiModels.ExistingProblem(x.getId(), x.getTitle(), x.getDescription()))
                .toList();
        List<AiModels.SimilarProblem> dupes = aiGateway.duplicates(p.getTitle(), p.getDescription(), existing);
        return dupes.stream().map(d -> toResponse(problemRepo.findById(d.id()).orElse(null))).toList();
    }

    public Problem getEntity(Long id) {
        return problemRepo.findById(id).orElseThrow(() -> ApiException.notFound("Problem not found"));
    }

    public List<ProblemCapability> capabilities(Long id) {
        return capabilityRepo.findByProblemId(id);
    }

    private void requireSubmitterOrAdmin(Problem problem, Long userId) {
        if (problem.getSubmitter() != null && problem.getSubmitter().getId().equals(userId)) {
            return;
        }
        if (currentUser.hasAnyRole(Roles.PROBLEM_ADMINS)) {
            return;
        }
        throw ApiException.forbidden("Only the submitter or an admin can re-run analysis for this problem");
    }

    private ProblemResponse toResponse(Problem p) {
        if (p == null) {
            return null;
        }
        List<ProblemCapability> caps = capabilityRepo.findByProblemId(p.getId());
        return new ProblemResponse(
                p.getId(), p.getTitle(), p.getDescription(),
                new ProblemResponse.UserBrief(p.getSubmitter().getId(), p.getSubmitter().getName(), p.getSubmitter().getEmail()),
                p.getCategory() != null
                        ? new ProblemResponse.CategoryBrief(p.getCategory().getId(), p.getCategory().getName()) : null,
                p.getLocation(), p.getEstimatedBeneficiaries(), p.getUrgency(),
                p.getRequiredOutcome(), p.getDocumentUrl(), p.getStatus(),
                p.getAiCategory(), p.getAiUrgency(), p.getAiConfidence(),
                p.getAiSkills(), p.getAiKeywords(), p.getAiExplanation(),
                p.getAiRootCauses(), p.getAiTargetBeneficiaries(), p.getAiChallenges(),
                p.getAiSolutionDirections(), p.getAiExpectedOutcomes(), p.getAiSource(),
                p.getAiSimilarity(),
                p.getSimilarProblem() != null
                        ? new ProblemResponse.DuplicateBrief(p.getSimilarProblem().getId(), p.getSimilarProblem().getTitle(), p.getAiSimilarity())
                        : null,
                caps.stream().map(c -> new ProblemResponse.CapabilityBrief(
                        c.getId(), c.getCapability(), c.getDescription(),
                        Arrays.asList((c.getRequiredSkills() == null ? "" : c.getRequiredSkills()).split(",\\s*")),
                        c.getPriority())).toList(),
                p.getCreatedAt() != null ? p.getCreatedAt().toString() : null
        );
    }
}