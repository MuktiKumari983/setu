package com.sih.innovationplatform.project;

import com.sih.innovationplatform.common.ApiException;
import com.sih.innovationplatform.common.CurrentUser;
import com.sih.innovationplatform.common.Roles;
import com.sih.innovationplatform.user.User;
import com.sih.innovationplatform.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectAccessService {

    private final ProjectRepository projectRepo;
    private final ProjectMemberRepository memberRepo;
    private final UserRepository userRepo;
    private final CurrentUser currentUser;

    public void requireMemberOrAdmin(Long projectId, Long userId) {
        if (currentUser.hasAnyRole(Roles.PLATFORM_ADMINS)) {
            return;
        }
        User user = userRepo.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> ApiException.notFound("Project not found"));
        boolean allowed = isMember(project, user)
                || (project.getCreatedBy() != null && project.getCreatedBy().getId().equals(userId))
                || (project.getProblem() != null && project.getProblem().getSubmitter() != null
                    && project.getProblem().getSubmitter().getId().equals(userId));
        if (!allowed) {
            throw ApiException.forbidden("You are not a member of this project");
        }
    }

    private boolean isMember(Project project, User user) {
        return memberRepo.findByProjectId(project.getId()).stream()
                .anyMatch(member -> member.getUser() != null && member.getUser().getId().equals(user.getId()));
    }
}
