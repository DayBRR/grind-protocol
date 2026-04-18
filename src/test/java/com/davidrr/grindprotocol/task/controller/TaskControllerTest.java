package com.davidrr.grindprotocol.task.controller;

import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import com.davidrr.grindprotocol.task.dto.CreateTaskFromTemplateRequest;
import com.davidrr.grindprotocol.task.dto.CreateTaskRequest;
import com.davidrr.grindprotocol.task.dto.TaskResponse;
import com.davidrr.grindprotocol.task.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.davidrr.grindprotocol.utils.TestAuthenticatedUserFactory.defaultUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TaskControllerTest {

    @Test
    @DisplayName("createTask debe delegar en el service")
    void createTask_shouldDelegateToService() {
        TaskService taskService = mock(TaskService.class);
        TaskController controller = new TaskController(taskService);

        AuthenticatedUser currentUser = defaultUser();
        CreateTaskRequest request = mock(CreateTaskRequest.class);
        TaskResponse response = mock(TaskResponse.class);

        when(taskService.createTask(1L, request)).thenReturn(response);

        TaskResponse result = controller.createTask(currentUser, request);

        assertThat(result).isSameAs(response);
        verify(taskService).createTask(1L, request);
    }

    @Test
    @DisplayName("getMyActiveTasks debe delegar en el service")
    void getMyActiveTasks_shouldDelegateToService() {
        TaskService taskService = mock(TaskService.class);
        TaskController controller = new TaskController(taskService);

        AuthenticatedUser currentUser = defaultUser();
        TaskResponse response = mock(TaskResponse.class);

        when(taskService.getActiveTasksByUser(1L)).thenReturn(List.of(response));

        List<TaskResponse> result = controller.getMyActiveTasks(currentUser);

        assertThat(result).containsExactly(response);
        verify(taskService).getActiveTasksByUser(1L);
    }

    @Test
    @DisplayName("getTaskById debe delegar en el service")
    void getTaskById_shouldDelegateToService() {
        TaskService taskService = mock(TaskService.class);
        TaskController controller = new TaskController(taskService);

        AuthenticatedUser currentUser = defaultUser();
        TaskResponse response = mock(TaskResponse.class);

        when(taskService.getTaskById(1L, 10L)).thenReturn(response);

        TaskResponse result = controller.getTaskById(currentUser, 10L);

        assertThat(result).isSameAs(response);
        verify(taskService).getTaskById(1L, 10L);
    }

    @Test
    @DisplayName("createTaskFromTemplate debe delegar en el service")
    void createTaskFromTemplate_shouldDelegateToService() {
        TaskService taskService = mock(TaskService.class);
        TaskController controller = new TaskController(taskService);

        AuthenticatedUser currentUser = defaultUser();
        CreateTaskFromTemplateRequest request = mock(CreateTaskFromTemplateRequest.class);
        TaskResponse response = mock(TaskResponse.class);

        when(taskService.createTaskFromTemplate(1L, request)).thenReturn(response);

        TaskResponse result = controller.createTaskFromTemplate(currentUser, request);

        assertThat(result).isSameAs(response);
        verify(taskService).createTaskFromTemplate(1L, request);
    }
}
