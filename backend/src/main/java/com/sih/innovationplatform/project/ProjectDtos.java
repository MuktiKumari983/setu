package com.sih.innovationplatform.project;

import java.util.List;

record ProjectCreateRequest(Long problemId, String description) {}

record StatusRequest(String status, String note) {}

record ProgressRequest(Integer progress) {}

record TaskRequest(String title, String description, Long assignedToId, String priority) {}

record TaskStatusRequest(String status) {}

record MilestoneRequest(String title, String description, String targetDate) {}

record MilestoneStatusRequest(String status) {}

record CommentRequest(String content, String commentType) {}

record DocumentRequest(String name, String url) {}

record FailureRequest(String stage, String reason, String missingCapability) {}

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