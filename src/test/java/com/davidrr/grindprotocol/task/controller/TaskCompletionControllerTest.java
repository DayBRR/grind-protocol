package com.davidrr.grindprotocol.task.controller;

import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import com.davidrr.grindprotocol.task.dto.CreateTaskCompletionRequest;
import com.davidrr.grindprotocol.task.dto.TaskCompletionResponse;
import com.davidrr.grindprotocol.task.service.TaskCompletionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.davidrr.grindprotocol.utils.TestAuthenticatedUserFactory.defaultUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TaskCompletionControllerTest {

    @Test
    @DisplayName("completeTask debe delegar en el service con el usuario autenticado y la request")
    void completeTask_shouldDelegateToService() {
        TaskCompletionService taskCompletionService = mock(TaskCompletionService.class);
        TaskCompletionController controller = new TaskCompletionController(taskCompletionService);

        AuthenticatedUser currentUser = defaultUser();
        CreateTaskCompletionRequest request = mock(CreateTaskCompletionRequest.class);
        TaskCompletionResponse response = mock(TaskCompletionResponse.class);

        when(taskCompletionService.completeTask(1L, request)).thenReturn(response);

        TaskCompletionResponse result = controller.completeTask(currentUser, request);

        assertThat(result).isSameAs(response);
        verify(taskCompletionService).completeTask(1L, request);
    }

    @Test
    @DisplayName("getTodayCompletions debe delegar en el service con el usuario autenticado")
    void getTodayCompletions_shouldDelegateToService() {
        TaskCompletionService taskCompletionService = mock(TaskCompletionService.class);
        TaskCompletionController controller = new TaskCompletionController(taskCompletionService);

        AuthenticatedUser currentUser = defaultUser();
        TaskCompletionResponse response = mock(TaskCompletionResponse.class);

        when(taskCompletionService.getTodayCompletions(1L)).thenReturn(List.of(response));

        List<TaskCompletionResponse> result = controller.getTodayCompletions(currentUser);

        assertThat(result).containsExactly(response);
        verify(taskCompletionService).getTodayCompletions(1L);
    }
}
