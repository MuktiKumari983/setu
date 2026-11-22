package com.sih.innovationplatform.project;

import com.sih.innovationplatform.ai.AiModels;
import com.sih.innovationplatform.common.CurrentUser;
import com.sih.innovationplatform.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final CurrentUser currentUser;

    @PostMapping
    public ProjectResponse create(@Valid @RequestBody ProjectCreateRequest request) {
        User user = currentUser.get();
        return projectService.create(request, user.getId());
    }

    @GetMapping
    public List<ProjectResponse> list() {
        User user = currentUser.get();
        return projectService.list(user.getId());
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable Long id) {
        User user = currentUser.get();
        return projectService.getForUser(id, user.getId());
    }

    @PutMapping("/{id}/status")
    public ProjectResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        User user = currentUser.get();
        return projectService.changeStatus(id, request, user.getId());
    }

    @PostMapping("/{id}/tasks")
    public ProjectResponse addTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        User user = currentUser.get();
        return projectService.addTask(id, request, user.getId());
    }

    @PutMapping("/{id}/tasks/{taskId}")
    public ProjectResponse updateTask(@PathVariable Long id, @PathVariable Long taskId,
                                      @Valid @RequestBody TaskStatusRequest request) {
        User user = currentUser.get();
        return projectService.updateTask(id, taskId, request, user.getId());
    }

    @PostMapping("/{id}/milestones")
    public ProjectResponse addMilestone(@PathVariable Long id, @Valid @RequestBody MilestoneRequest request) {
        User user = currentUser.get();
        return projectService.addMilestone(id, request, user.getId());
    }

    @PutMapping("/{id}/milestones/{milestoneId}")
    public ProjectResponse updateMilestone(@PathVariable Long id, @PathVariable Long milestoneId,
                                           @Valid @RequestBody MilestoneStatusRequest request) {
        User user = currentUser.get();
        return projectService.updateMilestone(id, milestoneId, request, user.getId());
    }

    @PostMapping("/{id}/comments")
    public ProjectResponse addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest request) {
        User user = currentUser.get();
        return projectService.addComment(id, request, user.getId());
    }

    @PostMapping("/{id}/documents")
    public ProjectResponse addDocument(@PathVariable Long id, @Valid @RequestBody DocumentRequest request) {
        User user = currentUser.get();
        return projectService.addDocument(id, request, user.getId());
    }

    @PutMapping("/{id}/progress")
    public ProjectResponse updateProgress(@PathVariable Long id, @Valid @RequestBody ProgressRequest request) {
        User user = currentUser.get();
        return projectService.updateProgress(id, request, user.getId());
    }

    @PostMapping("/{id}/failure")
    public ProjectResponse reportFailure(@PathVariable Long id, @Valid @RequestBody FailureRequest request) {
        User user = currentUser.get();
        return projectService.reportFailure(id, request, user.getId());
    }

    @PostMapping("/{id}/failure/resolve")
    public ProjectResponse resolveFailure(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        User user = currentUser.get();
        return projectService.resolveFailure(id, body == null ? null : body.get("note"), user.getId());
    }

    @GetMapping("/{id}/recovery-recommendations")
    public AiModels.RecoveryRecommendation recovery(@PathVariable Long id) {
        return projectService.recoveryRecommendations(id, currentUser.get().getId());
    }

    @GetMapping("/{id}/timeline")
    public List<ProjectResponse.HistoryBrief> timeline(@PathVariable Long id) {
        User user = currentUser.get();
        return projectService.getForUser(id, user.getId()).history();
    }
}