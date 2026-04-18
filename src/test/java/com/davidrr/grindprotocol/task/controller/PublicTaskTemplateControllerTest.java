package com.davidrr.grindprotocol.task.controller;

import com.davidrr.grindprotocol.task.dto.TaskTemplateResponse;
import com.davidrr.grindprotocol.task.service.TaskTemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PublicTaskTemplateControllerTest {

    @Test
    @DisplayName("getPublicTemplates debe delegar en el service")
    void getPublicTemplates_shouldDelegateToService() {
        TaskTemplateService taskTemplateService = mock(TaskTemplateService.class);
        PublicTaskTemplateController controller = new PublicTaskTemplateController(taskTemplateService);

        TaskTemplateResponse response = mock(TaskTemplateResponse.class);
        when(taskTemplateService.getPublicActiveTemplates()).thenReturn(List.of(response));

        List<TaskTemplateResponse> result = controller.getPublicTemplates();

        assertThat(result).containsExactly(response);
        verify(taskTemplateService).getPublicActiveTemplates();
    }

    @Test
    @DisplayName("getPublicTemplateById debe delegar en el service")
    void getPublicTemplateById_shouldDelegateToService() {
        TaskTemplateService taskTemplateService = mock(TaskTemplateService.class);
        PublicTaskTemplateController controller = new PublicTaskTemplateController(taskTemplateService);

        TaskTemplateResponse response = mock(TaskTemplateResponse.class);
        when(taskTemplateService.getPublicTemplateById(10L)).thenReturn(response);

        TaskTemplateResponse result = controller.getPublicTemplateById(10L);

        assertThat(result).isSameAs(response);
        verify(taskTemplateService).getPublicTemplateById(10L);
    }
}
