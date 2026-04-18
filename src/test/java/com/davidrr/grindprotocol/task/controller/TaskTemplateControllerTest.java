package com.davidrr.grindprotocol.task.controller;

import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import com.davidrr.grindprotocol.task.dto.CreateTaskTemplateRequest;
import com.davidrr.grindprotocol.task.dto.TaskTemplateResponse;
import com.davidrr.grindprotocol.task.service.TaskTemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.davidrr.grindprotocol.utils.TestAuthenticatedUserFactory.defaultUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TaskTemplateControllerTest {

    @Test
    @DisplayName("createTemplate debe delegar en el service")
    void createTemplate_shouldDelegateToService() {
        TaskTemplateService taskTemplateService = mock(TaskTemplateService.class);
        TaskTemplateController controller = new TaskTemplateController(taskTemplateService);

        AuthenticatedUser currentUser = defaultUser();
        CreateTaskTemplateRequest request = mock(CreateTaskTemplateRequest.class);
        TaskTemplateResponse response = mock(TaskTemplateResponse.class);

        when(taskTemplateService.createTemplate(1L, request)).thenReturn(response);

        TaskTemplateResponse result = controller.createTemplate(currentUser, request);

        assertThat(result).isSameAs(response);
        verify(taskTemplateService).createTemplate(1L, request);
    }

    @Test
    @DisplayName("getMyTemplates debe delegar en el service")
    void getMyTemplates_shouldDelegateToService() {
        TaskTemplateService taskTemplateService = mock(TaskTemplateService.class);
        TaskTemplateController controller = new TaskTemplateController(taskTemplateService);

        AuthenticatedUser currentUser = defaultUser();
        TaskTemplateResponse response = mock(TaskTemplateResponse.class);

        when(taskTemplateService.getMyActiveTemplates(1L)).thenReturn(List.of(response));

        List<TaskTemplateResponse> result = controller.getMyTemplates(currentUser);

        assertThat(result).containsExactly(response);
        verify(taskTemplateService).getMyActiveTemplates(1L);
    }

    @Test
    @DisplayName("getMyTemplateById debe delegar en el service")
    void getMyTemplateById_shouldDelegateToService() {
        TaskTemplateService taskTemplateService = mock(TaskTemplateService.class);
        TaskTemplateController controller = new TaskTemplateController(taskTemplateService);

        AuthenticatedUser currentUser = defaultUser();
        TaskTemplateResponse response = mock(TaskTemplateResponse.class);

        when(taskTemplateService.getMyTemplateById(1L, 10L)).thenReturn(response);

        TaskTemplateResponse result = controller.getMyTemplateById(currentUser, 10L);

        assertThat(result).isSameAs(response);
        verify(taskTemplateService).getMyTemplateById(1L, 10L);
    }
}
