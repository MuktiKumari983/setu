package com.sih.innovationplatform.project;

import com.sih.innovationplatform.ai.AiModels;
import com.sih.innovationplatform.common.ApiException;
import com.sih.innovationplatform.common.CurrentUser;
import com.sih.innovationplatform.industry.IndustryRepository;
import com.sih.innovationplatform.match.MatchRepository;
import com.sih.innovationplatform.match.MatchingService;
import com.sih.innovationplatform.problem.Problem;
import com.sih.innovationplatform.problem.ProblemRepository;
import com.sih.innovationplatform.project.ProjectResponse.MemberBrief;
import com.sih.innovationplatform.project.ProjectResponse.TaskBrief;
import com.sih.innovationplatform.project.ProjectResponse.MilestoneBrief;
import com.sih.innovationplatform.project.ProjectResponse.CommentBrief;
import com.sih.innovationplatform.project.ProjectResponse.DocumentBrief;
import com.sih.innovationplatform.project.ProjectResponse.HistoryBrief;
import com.sih.innovationplatform.project.ProjectResponse.FailureBrief;
import com.sih.innovationplatform.project.ProjectResponse.StakeholderBrief;
import com.sih.innovationplatform.team.StudentTeam;
import com.sih.innovationplatform.team.StudentTeamRepository;
import com.sih.innovationplatform.university.FacultyRepository;
import com.sih.innovationplatform.university.UniversityRepository;
import com.sih.innovationplatform.user.User;
import com.sih.innovationplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepo;
    private final ProjectMemberRepository memberRepo;
    private final TaskRepository taskRepo;
    private final MilestoneRepository milestoneRepo;
    private final ProjectCommentRepository commentRepo;
    private final DocumentRepository documentRepo;
    private final ProjectStatusHistoryRepository historyRepo;
    private final FailureRecordRepository failureRepo;
    private final ProblemRepository problemRepo;
    private final UserRepository userRepo;
    private final MatchRepository matchRepo;
    private final UniversityRepository universityRepo;
    private final StudentTeamRepository teamRepo;
    private final FacultyRepository facultyRepo;
    private final IndustryRepository industryRepo;
    private final MatchingService matchingService;
    private final CurrentUser currentUser;

    private static final Map<String, Integer> STAGE_INDEX = new HashMap<>();
    private static final Map<String, Integer> STAGE_PROGRESS = new HashMap<>();

    static {
        List<String> stages = Project.STAGES;
        for (int i = 0; i < stages.size(); i++) {
            STAGE_INDEX.put(stages.get(i), i);
            STAGE_PROGRESS.put(stages.get(i), List.of(0, 10, 20, 25, 35, 45, 55, 65, 75, 100).get(i));
        }
    }

    @Transactional
    public ProjectResponse create(ProjectCreateRequest req, Long userId) {
        User actor = userRepo.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
        Problem problem = problemRepo.findById(req.problemId())
                .orElseThrow(() -> ApiException.notFound("Problem not found"));
        if (!List.of(Problem.STATUS_VERIFIED, Problem.STATUS_MATCHED).contains(problem.getStatus())) {
            throw ApiException.badRequest("Problem must be VERIFIED before creating a project");
        }

        Project project = new Project();
        project.setProblem(problem);
        project.setTitle(problem.getTitle());
        project.setDescription(req.description() != null ? req.description()
                : "Project workspace created for \"" + problem.getTitle() + "\"");
        project.setCreatedBy(actor);
        project.setStatus(problem.getStatus());
        project.setProgressPercent(STAGE_PROGRESS.getOrDefault(project.getStatus(), 0));

        // Attach recommended stakeholders from stored matches
        attachMatch(project, "UNIVERSITY");
        attachMatch(project, "TEAM");
        attachMatch(project, "MENTOR");
        attachMatch(project, "INDUSTRY");

        Project saved = projectRepo.save(project);
        addHistory(saved, saved.getStatus(), "Project created from verified challenge", actor);
        addMember(saved, actor, "COORDINATOR");
        if (!problem.getSubmitter().getId().equals(actor.getId())) {
            addMember(saved, problem.getSubmitter(), "CITIZEN");
        }
        if (saved.getTeam() != null && saved.getTeam().getLeadId() != null) {
            userRepo.findById(saved.getTeam().getLeadId()).ifPresent(u -> addMember(saved, u, "TEAM_LEAD"));
        }
        return get(saved.getId());
    }

    private void attachMatch(Project project, String type) {
        com.sih.innovationplatform.match.Match m = matchRepo
                .findByProblemIdAndEntityTypeOrderByScoreDesc(project.getProblem().getId(), type).stream()
                .findFirst().orElse(null);
        if (m == null) {
            return;
        }
        switch (type) {
            case "UNIVERSITY" -> universityRepo.findById(m.getEntityId()).ifPresent(project::setUniversity);
            case "TEAM" -> teamRepo.findById(m.getEntityId()).ifPresent(project::setTeam);
            case "MENTOR" -> facultyRepo.findById(m.getEntityId()).ifPresent(project::setLeadMentor);
            case "INDUSTRY" -> industryRepo.findById(m.getEntityId()).ifPresent(project::setIndustry);
            default -> { }
        }
    }

    public List<ProjectResponse> list(Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
        List<Project> all = projectRepo.findAllByOrderByCreatedAtDesc();
        if (List.of("PLATFORM_ADMIN", "GOVERNMENT_ADMIN", "INDUSTRY").contains(user.getRoleName())) {
            return all.stream().map(this::toResponse).toList();
        }
        if ("UNIVERSITY_ADMIN".equals(user.getRoleName())) {
            return all.stream()
                    .filter(p -> p.getUniversity() != null
                            && p.getUniversity().getId().equals(user.getLinkedUniversityId())
                            || isMember(p, user) || isCreator(p, user))
                    .map(this::toResponse).toList();
        }
        return all.stream()
                .filter(p -> isMember(p, user) || isCreator(p, user)
                        || p.getProblem().getSubmitter().getId().equals(user.getId()))
                .map(this::toResponse).toList();
    }

    private boolean isMember(Project p, User user) {
        return memberRepo.findByProjectId(p.getId()).stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));
    }

    private boolean isCreator(Project p, User user) {
        return p.getCreatedBy() != null && p.getCreatedBy().getId().equals(user.getId());
    }

    public ProjectResponse get(Long id) {
        Project p = projectRepo.findById(id).orElseThrow(() -> ApiException.notFound("Project not found"));
        return toResponse(p);
    }

    private ProjectResponse toResponse(Project p) {
        List<MemberBrief> members = memberRepo.findByProjectId(p.getId()).stream()
                .map(m -> new MemberBrief(m.getUser().getId(), m.getUser().getName(), m.getUser().getEmail(), m.getRoleInProject()))
                .toList();
        List<TaskBrief> tasks = taskRepo.findByProjectIdOrderByCreatedAtDesc(p.getId()).stream()
                .map(t -> new TaskBrief(t.getId(), t.getTitle(), t.getDescription(),
                        t.getAssignedTo() == null ? null : t.getAssignedTo().getId(),
                        t.getAssignedTo() == null ? null : t.getAssignedTo().getName(),
                        t.getStatus(), t.getPriority(),
                        t.getDueDate() == null ? null : t.getDueDate().toString()))
                .toList();
        List<MilestoneBrief> milestones = milestoneRepo.findByProjectIdOrderByTargetDateAsc(p.getId()).stream()
                .map(m -> new MilestoneBrief(m.getId(), m.getTitle(), m.getDescription(), m.getStatus(),
                        m.getTargetDate() == null ? null : m.getTargetDate().toString(),
                        m.getCompletedAt() == null ? null : m.getCompletedAt().toString()))
                .toList();
        List<CommentBrief> comments = commentRepo.findByProjectIdOrderByCreatedAtAsc(p.getId()).stream()
                .map(c -> new CommentBrief(c.getId(), c.getAuthor().getId(), c.getAuthor().getName(),
                        c.getCommentType(), c.getContent(),
                        c.getCreatedAt() == null ? null : c.getCreatedAt().toString()))
                .toList();
        List<DocumentBrief> documents = documentRepo.findByProjectIdOrderByCreatedAtDesc(p.getId()).stream()
                .map(d -> new DocumentBrief(d.getId(), d.getName(), d.getUrl(),
                        d.getUploader() == null ? "" : d.getUploader().getName(),
                        d.getCreatedAt() == null ? null : d.getCreatedAt().toString()))
                .toList();
        List<HistoryBrief> history = historyRepo.findByProjectIdOrderByChangedAtAsc(p.getId()).stream()
                .map(h -> new HistoryBrief(h.getStatus(), h.getNote(),
                        h.getChangedBy() == null ? "" : h.getChangedBy().getName(),
                        h.getChangedAt() == null ? null : h.getChangedAt().toString()))
                .toList();
        List<FailureBrief> failures = failureRepo.findByProjectIdOrderByCreatedAtDesc(p.getId()).stream()
                .map(f -> new FailureBrief(f.getId(), f.getStage(), f.getReason(),
                        f.getMissingCapability(), f.getResolved(),
                        f.getCreatedAt() == null ? null : f.getCreatedAt().toString()))
                .toList();

        return new ProjectResponse(
                p.getId(), p.getTitle(), p.getDescription(),
                p.getProblem().getId(), p.getProblem().getTitle(),
                p.getStatus(), p.getProgressPercent(), p.getFailureReason(),
                p.getUniversity() != null ? new StakeholderBrief(p.getUniversity().getId(), p.getUniversity().getName()) : null,
                p.getTeam() != null ? new StakeholderBrief(p.getTeam().getId(), p.getTeam().getName()) : null,
                p.getLeadMentor() != null ? new StakeholderBrief(p.getLeadMentor().getId(), p.getLeadMentor().getName()) : null,
                p.getIndustry() != null ? new StakeholderBrief(p.getIndustry().getId(), p.getIndustry().getName()) : null,
                p.getCreatedBy() != null ? new MemberBrief(p.getCreatedBy().getId(), p.getCreatedBy().getName(), p.getCreatedBy().getEmail(), "COORDINATOR") : null,
                p.getCreatedAt() == null ? null : p.getCreatedAt().toString(),
                p.getUpdatedAt() == null ? null : p.getUpdatedAt().toString(),
                members, tasks, milestones, comments, documents, history, failures);
    }

    @Transactional
    public ProjectResponse changeStatus(Long id, StatusRequest req, Long userId) {
        User actor = userRepo.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
        Project p = projectRepo.findById(id).orElseThrow(() -> ApiException.notFound("Project not found"));
        String target = req.status() == null ? null : req.status().toUpperCase();

        if (Project.CANCELLED.equals(target) || Project.COMPLETED.equals(target)) {
            if (!p.getStatus().equals(target)) {
                p.setStatus(target);
                addHistory(p, target, req.note(), actor);
            }
            return saveAndResponse(p);
        }
        if (Project.NEEDS_SUPPORT.equals(target)) {
            return saveAndResponse(p); // set via failure endpoint only
        }

        int current = STAGE_INDEX.getOrDefault(p.getStatus(), 0);
        if (STAGE_INDEX.containsKey(target)) {
            if (STAGE_INDEX.get(target) < current && !"NEEDS_SUPPORT".equals(p.getStatus())) {
                throw ApiException.badRequest("Illegal lifecycle transition from " + p.getStatus() + " to " + target);
            }
            if (STAGE_INDEX.get(target) < current && "NEEDS_SUPPORT".equals(p.getStatus())) {
                // recovery path: allow returning to the stage the project failed at (already in index)
            }
            p.setStatus(target);
            p.setProgressPercent(STAGE_PROGRESS.get(target));
        } else {
            throw ApiException.badRequest("Unknown lifecycle stage: " + target);
        }

        if (Problem.STATUS_VERIFIED.equals(p.getProblem().getStatus())
                && STAGE_INDEX.getOrDefault(target, 0) >= STAGE_INDEX.get("TEAM_FORMED")) {
            p.getProblem().setStatus(Problem.STATUS_MATCHED);
            problemRepo.save(p.getProblem());
        }
        p.setFailureReason(null);
        addHistory(p, target, req.note(), actor);
        return saveAndResponse(p);
    }

    @Transactional
    public ProjectResponse reportFailure(Long id, FailureRequest req, Long userId) {
        User actor = userRepo.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
        Project p = projectRepo.findById(id).orElseThrow(() -> ApiException.notFound("Project not found"));

        FailureRecord rec = new FailureRecord();
        rec.setProject(p);
        rec.setStage(req.stage() != null ? req.stage().toUpperCase() : p.getStatus());
        rec.setReason(req.reason());
        rec.setMissingCapability(req.missingCapability());
        rec.setReportedBy(actor);
        rec.setResolved(false);
        failureRepo.save(rec);

        p.setStatus(Project.NEEDS_SUPPORT);
        p.setFailureReason(req.reason());
        p.setUpdatedAt(LocalDateTime.now());
        projectRepo.save(p);
        addHistory(p, Project.NEEDS_SUPPORT, "Failed at " + rec.getStage() + ": " + req.reason(), actor);
        return get(id);
    }

    @Transactional
    public ProjectResponse resolveFailure(Long id, String note, Long userId) {
        User actor = userRepo.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
        Project p = projectRepo.findById(id).orElseThrow(() -> ApiException.notFound("Project not found"));
        FailureRecord latest = failureRepo.findByProjectIdOrderByCreatedAtDesc(id).stream()
                .findFirst().orElse(null);
        if (latest == null) {
            throw ApiException.badRequest("No failure record found");
        }
        String resumeStage = latest.getStage();
        if (!STAGE_INDEX.containsKey(resumeStage)) {
            resumeStage = Project.STAGES.get(3); // TEAM_FORMED
        }
        latest.setResolved(true);
        failureRepo.save(latest);
        p.setStatus(resumeStage);
        p.setProgressPercent(STAGE_PROGRESS.getOrDefault(resumeStage, 0));
        p.setFailureReason(null);
        p.setUpdatedAt(LocalDateTime.now());
        projectRepo.save(p);
        addHistory(p, resumeStage, "Failure resolved, project resumed" + (note != null ? " - " + note : ""), actor);
        return get(id);
    }

    public AiModels.RecoveryRecommendation recoveryRecommendations(Long id) {
        Project p = projectRepo.findById(id).orElseThrow(() -> ApiException.notFound("Project not found"));
        FailureRecord latest = failureRepo.findByProjectIdOrderByCreatedAtDesc(id).stream()
                .findFirst().orElse(null);
        String missing = latest != null ? latest.getMissingCapability() : "GIS";
        return matchingService.recovery(p.getProblem().getId(), missing);
    }

    public List<ProjectResponse> timelineStatus() {
        List<String> active = Project.STAGES.stream().skip(1).toList();
        return projectRepo.findByStatusOrderByCreatedAtDesc("NEEDS_SUPPORT").stream()
                .map(this::toResponse).toList();
    }

    // ---- workspace helpers ----

    @Transactional
    public ProjectResponse addTask(Long id, TaskRequest req, Long userId) {
        requireMemberOrAdmin(id, userId);
        Project p = projectRepo.findById(id).orElseThrow();
        Task t = new Task();
        t.setProject(p);
        t.setTitle(req.title());
        t.setDescription(req.description());
        t.setPriority(req.priority() == null ? "MEDIUM" : req.priority());
        t.setAssignedTo(req.assignedToId() == null ? null : userRepo.findById(req.assignedToId()).orElse(null));
        taskRepo.save(t);
        return get(id);
    }

    @Transactional
    public ProjectResponse updateTask(Long projectId, Long taskId, TaskStatusRequest req, Long userId) {
        requireMemberOrAdmin(projectId, userId);
        Task t = taskRepo.findById(taskId).orElseThrow(() -> ApiException.notFound("Task not found"));
        t.setStatus(req.status() == null ? t.getStatus() : req.status().toUpperCase());
        if ("DONE".equals(t.getStatus())) {
            t.setStatus("DONE");
        }
        taskRepo.save(t);
        return get(projectId);
    }

    @Transactional
    public ProjectResponse addMilestone(Long id, MilestoneRequest req, Long userId) {
        requireMemberOrAdmin(id, userId);
        Project p = projectRepo.findById(id).orElseThrow();
        Milestone m = new Milestone();
        m.setProject(p);
        m.setTitle(req.title());
        m.setDescription(req.description());
        m.setTargetDate(req.targetDate() == null ? null : LocalDate.parse(req.targetDate()));
        milestoneRepo.save(m);
        return get(id);
    }

    @Transactional
    public ProjectResponse updateMilestone(Long projectId, Long milestoneId, MilestoneStatusRequest req, Long userId) {
        requireMemberOrAdmin(projectId, userId);
        Milestone m = milestoneRepo.findById(milestoneId).orElseThrow(() -> ApiException.notFound("Milestone not found"));
        if ("COMPLETED".equalsIgnoreCase(req.status())) {
            m.markCompleted();
        } else if ("IN_PROGRESS".equalsIgnoreCase(req.status())) {
            m.setStatus("IN_PROGRESS");
        } else if ("PENDING".equalsIgnoreCase(req.status())) {
            m.setStatus("PENDING");
            m.setCompletedAt(null);
        }
        milestoneRepo.save(m);
        return get(projectId);
    }

    @Transactional
    public ProjectResponse addComment(Long id, CommentRequest req, Long userId) {
        requireMemberOrAdmin(id, userId);
        Project p = projectRepo.findById(id).orElseThrow();
        User author = userRepo.findById(userId).orElseThrow();
        ProjectComment c = new ProjectComment();
        c.setProject(p);
        c.setAuthor(author);
        c.setCommentType(req.commentType() == null ? "GENERAL" : req.commentType().toUpperCase());
        c.setContent(req.content());
        commentRepo.save(c);
        return get(id);
    }

    @Transactional
    public ProjectResponse addDocument(Long id, DocumentRequest req, Long userId) {
        requireMemberOrAdmin(id, userId);
        Project p = projectRepo.findById(id).orElseThrow();
        User user = userRepo.findById(userId).orElse(null);
        Document d = new Document();
        d.setProject(p);
        d.setName(req.name());
        d.setUrl(req.url());
        d.setUploader(user);
        documentRepo.save(d);
        return get(id);
    }

    @Transactional
    public ProjectResponse updateProgress(Long id, ProgressRequest req, Long userId) {
        requireMemberOrAdmin(id, userId);
        Project p = projectRepo.findById(id).orElseThrow();
        p.setProgressPercent(Math.max(0, Math.min(100, req.progress())));
        p.setUpdatedAt(LocalDateTime.now());
        return saveAndResponse(p);
    }

    // ---- internal helpers ----

    private ProjectResponse saveAndResponse(Project p) {
        p.setUpdatedAt(LocalDateTime.now());
        Project saved = projectRepo.save(p);
        return get(saved.getId());
    }

    private void addHistory(Project p, String status, String note, User actor) {
        ProjectStatusHistory h = new ProjectStatusHistory();
        h.setProject(p);
        h.setStatus(status);
        h.setNote(note);
        h.setChangedBy(actor);
        historyRepo.save(h);
    }

    private void addMember(Project p, User u, String role) {
        Set<Long> existing = new HashSet<>(memberRepo.findByProjectId(p.getId())
                .stream().map(m -> m.getUser().getId()).toList());
        if (existing.contains(u.getId())) {
            return;
        }
        ProjectMember m = new ProjectMember();
        m.setProject(p);
        m.setUser(u);
        m.setRoleInProject(role);
        memberRepo.save(m);
    }

    private void requireMemberOrAdmin(Long projectId, Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
        if (currentUser.hasAnyRole("PLATFORM_ADMIN", "GOVERNMENT_ADMIN")) {
            return;
        }
        Project p = projectRepo.findById(projectId).orElseThrow(() -> ApiException.notFound("Project not found"));
        boolean member = isMember(p, user) || isCreator(p, user)
                || p.getProblem().getSubmitter().getId().equals(userId);
        if (!member) {
            throw ApiException.forbidden("You are not a member of this project");
        }
    }
}