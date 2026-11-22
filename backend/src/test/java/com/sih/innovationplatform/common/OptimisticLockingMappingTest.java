package com.sih.innovationplatform.common;

import com.sih.innovationplatform.impact.ImpactEvidence;
import com.sih.innovationplatform.impact.ImpactRecord;
import com.sih.innovationplatform.industry.Industry;
import com.sih.innovationplatform.industry.IndustryCapability;
import com.sih.innovationplatform.match.Match;
import com.sih.innovationplatform.notification.Notification;
import com.sih.innovationplatform.problem.Problem;
import com.sih.innovationplatform.problem.ProblemCapability;
import com.sih.innovationplatform.problem.ProblemCategory;
import com.sih.innovationplatform.project.Document;
import com.sih.innovationplatform.project.FailureRecord;
import com.sih.innovationplatform.project.Milestone;
import com.sih.innovationplatform.project.Project;
import com.sih.innovationplatform.project.ProjectComment;
import com.sih.innovationplatform.project.ProjectMember;
import com.sih.innovationplatform.project.ProjectStatusHistory;
import com.sih.innovationplatform.project.Task;
import com.sih.innovationplatform.team.StudentTeam;
import com.sih.innovationplatform.team.TeamMember;
import com.sih.innovationplatform.university.Department;
import com.sih.innovationplatform.university.Faculty;
import com.sih.innovationplatform.university.University;
import com.sih.innovationplatform.user.Role;
import com.sih.innovationplatform.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptimisticLockingMappingTest {

    private static final Class<?>[] ENTITIES = {
            Role.class, User.class, ProblemCategory.class, ProblemCapability.class, Problem.class,
            University.class, Department.class, Faculty.class, StudentTeam.class, TeamMember.class,
            Industry.class, IndustryCapability.class, Project.class, ProjectMember.class, Task.class,
            Milestone.class, ProjectComment.class, Document.class, ProjectStatusHistory.class,
            FailureRecord.class, Match.class, ImpactRecord.class, ImpactEvidence.class, Notification.class
    };

    @Test
    void everyMappedEntityHasOptimisticLocking() {
        for (Class<?> entity : ENTITIES) {
            assertNotNull(entity.getAnnotation(Entity.class), entity.getSimpleName());
            assertTrue(java.util.Arrays.stream(entity.getDeclaredFields())
                    .anyMatch(field -> field.isAnnotationPresent(Version.class)), entity.getSimpleName());
        }
    }

    @Test
    void previouslyUnmappedTablesAreMappedWithUniqueConstraints() {
        Table teamTable = TeamMember.class.getAnnotation(Table.class);
        assertEquals("team_members", teamTable.name());
        assertEquals(1, teamTable.uniqueConstraints().length);

        Table capabilityTable = IndustryCapability.class.getAnnotation(Table.class);
        assertEquals("industry_capabilities", capabilityTable.name());
        assertEquals(1, capabilityTable.uniqueConstraints().length);
    }
}
