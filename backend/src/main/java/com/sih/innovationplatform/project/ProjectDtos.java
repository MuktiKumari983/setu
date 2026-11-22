package com.sih.innovationplatform.project;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

record ProjectCreateRequest(
        @NotNull(message = "Problem is required")
        @Positive(message = "Problem is required")
        Long problemId,
        @Size(max = 2000, message = "Description must be 2000 characters or fewer")
        String description
) {}

record StatusRequest(
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "^(SUBMITTED|VERIFIED|MATCHED|TEAM_FORMED|PROPOSAL|PROTOTYPE|REVIEW|PILOT|IMPLEMENTATION|IMPACT|NEEDS_SUPPORT|COMPLETED|CANCELLED)$", message = "Invalid project status")
        String status,
        @Size(max = 1000, message = "Note must be 1000 characters or fewer")
        String note
) {}

record ProgressRequest(
        @NotNull(message = "Progress is required")
        @Min(value = 0, message = "Progress cannot be negative")
        @Max(value = 100, message = "Progress cannot exceed 100")
        Integer progress
) {}

record TaskRequest(
        @NotBlank(message = "Task title is required")
        @Size(max = 250, message = "Task title must be 250 characters or fewer")
        String title,
        @Size(max = 2000, message = "Task description must be 2000 characters or fewer")
        String description,
        @Positive(message = "Invalid assigned user")
        Long assignedToId,
        @Pattern(regexp = "^(LOW|MEDIUM|HIGH)?$", message = "Invalid priority")
        String priority
) {}

record TaskStatusRequest(
        @NotBlank(message = "Task status is required")
        @Pattern(regexp = "^(OPEN|IN_PROGRESS|DONE|BLOCKED)$", message = "Invalid task status")
        String status
) {}

record MilestoneRequest(
        @NotBlank(message = "Milestone title is required")
        @Size(max = 250, message = "Milestone title must be 250 characters or fewer")
        String title,
        @Size(max = 1500, message = "Milestone description must be 1500 characters or fewer")
        String description,
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Target date must be YYYY-MM-DD")
        String targetDate
) {}

record MilestoneStatusRequest(
        @NotBlank(message = "Milestone status is required")
        @Pattern(regexp = "^(PENDING|IN_PROGRESS|COMPLETED)$", message = "Invalid milestone status")
        String status
) {}

record CommentRequest(
        @NotBlank(message = "Comment is required")
        @Size(max = 4000, message = "Comment must be 4000 characters or fewer")
        String content,
        @Pattern(regexp = "^(GENERAL|MENTOR|INDUSTRY)?$", message = "Invalid comment type")
        String commentType
) {}

record DocumentRequest(
        @NotBlank(message = "Document name is required")
        @Size(max = 250, message = "Document name must be 250 characters or fewer")
        String name,
        @Size(max = 500, message = "Document URL must be 500 characters or fewer")
        String url
) {}

record FailureRequest(
        @Size(max = 30, message = "Stage must be 30 characters or fewer")
        String stage,
        @NotBlank(message = "Failure reason is required")
        @Size(max = 2000, message = "Failure reason must be 2000 characters or fewer")
        String reason,
        @Size(max = 300, message = "Missing capability must be 300 characters or fewer")
        String missingCapability
) {}

record ProjectResponse(
        Long id,
        String title,
        String description,
        Long problemId,
        String problemTitle,
        String status,
        Integer progressPercent,
        String failureReason,
        StakeholderBrief university,
        StakeholderBrief team,
        StakeholderBrief mentor,
        StakeholderBrief industry,
        MemberBrief createdBy,
        String createdAt,
        String updatedAt,
        List<MemberBrief> members,
        List<TaskBrief> tasks,
        List<MilestoneBrief> milestones,
        List<CommentBrief> comments,
        List<DocumentBrief> documents,
        List<HistoryBrief> history,
        List<FailureBrief> failureRecords
) {
    record StakeholderBrief(Long id, String name) {}
    record MemberBrief(Long id, String name, String email, String role) {}
    record TaskBrief(Long id, String title, String description, Long assignedToId,
                     String assignedToName, String status, String priority, String dueDate) {}
    record MilestoneBrief(Long id, String title, String description, String status,
                          String targetDate, String completedAt) {}
    record CommentBrief(Long id, Long authorId, String author, String commentType,
                        String content, String createdAt) {}
    record DocumentBrief(Long id, String name, String url, String uploader, String createdAt) {}
    record HistoryBrief(String status, String note, String changedBy, String changedAt) {}
    record FailureBrief(Long id, String stage, String reason, String missingCapability,
                        Boolean resolved, String createdAt) {}
}