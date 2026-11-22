package com.sih.innovationplatform.project;

import com.sih.innovationplatform.common.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectChildScopeTest {

    @Mock private ProjectAccessService projectAccessService;
    @Mock private TaskRepository taskRepo;
    @Mock private MilestoneRepository milestoneRepo;
    @InjectMocks private ProjectService service;

    @Test
    void taskFromAnotherProjectIsNotFound() {
        when(taskRepo.findByIdAndProjectId(7L, 1L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> service.updateTask(1L, 7L, new TaskStatusRequest("DONE"), 9L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(taskRepo).findByIdAndProjectId(7L, 1L);
        verify(taskRepo, never()).findById(any());
    }

    @Test
    void milestoneFromAnotherProjectIsNotFound() {
        when(milestoneRepo.findByIdAndProjectId(8L, 1L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> service.updateMilestone(1L, 8L, new MilestoneStatusRequest("COMPLETED"), 9L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(milestoneRepo).findByIdAndProjectId(8L, 1L);
        verify(milestoneRepo, never()).findById(any());
    }
}
