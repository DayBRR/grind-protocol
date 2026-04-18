package com.davidrr.grindprotocol.task.service;

import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.task.dto.CreateTaskTemplateRequest;
import com.davidrr.grindprotocol.task.dto.TaskTemplateResponse;
import com.davidrr.grindprotocol.task.mapper.TaskTemplateMapper;
import com.davidrr.grindprotocol.task.model.TaskTemplate;
import com.davidrr.grindprotocol.task.model.Trait;
import com.davidrr.grindprotocol.task.repository.TaskTemplateRepository;
import com.davidrr.grindprotocol.task.repository.TraitRepository;
import com.davidrr.grindprotocol.task.service.impl.TaskTemplateServiceImpl;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.user.repository.UserRepository;
import com.davidrr.grindprotocol.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskTemplateServiceImplTest {

    @Mock
    private TaskTemplateRepository taskTemplateRepository;
    @Mock
    private TraitRepository traitRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskTemplateMapper taskTemplateMapper;

    @InjectMocks
    private TaskTemplateServiceImpl taskTemplateService;

    private User user;

    @BeforeEach
    void setUp() {
        user = TestDataFactory.user();
    }

    @Test
    @DisplayName("createTemplate debe crear plantilla normalizando traits")
    void createTemplate_shouldCreateTemplateWithNormalizedTraits() {
        CreateTaskTemplateRequest request = mock(CreateTaskTemplateRequest.class);
        when(request.getTitle()).thenReturn("  Template  ");
        when(request.getDescription()).thenReturn("Desc");
        when(request.getCategory()).thenReturn(null);
        when(request.getDifficulty()).thenReturn(null);
        when(request.getTaskType()).thenReturn(null);
        when(request.getBaseXp()).thenReturn(80);
        when(request.isMandatory()).thenReturn(true);
        when(request.isStreakEligible()).thenReturn(true);
        when(request.isRepeatable()).thenReturn(true);
        when(request.getMaxCompletionsPerDay()).thenReturn(3);
        when(request.isDiminishingReturnsEnabled()).thenReturn(true);
        when(request.getDueTime()).thenReturn(null);
        when(request.getWeeklyClosingDay()).thenReturn(null);
        when(request.isPublicTemplate()).thenReturn(true);
        when(request.isActive()).thenReturn(true);
        when(request.getTraitCodes()).thenReturn(Set.of("focus", " discipline "));

        Trait focus = Trait.builder().id(1L).code("FOCUS").active(true).build();
        Trait discipline = Trait.builder().id(2L).code("DISCIPLINE").active(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(traitRepository.findByCodeInAndActiveTrue(Set.of("FOCUS", "DISCIPLINE")))
                .thenReturn(List.of(focus, discipline));

        ArgumentCaptor<TaskTemplate> templateCaptor = ArgumentCaptor.forClass(TaskTemplate.class);
        when(taskTemplateRepository.save(templateCaptor.capture())).thenAnswer(invocation -> {
            TaskTemplate template = invocation.getArgument(0);
            template.setId(100L);
            return template;
        });

        TaskTemplateResponse response = mock(TaskTemplateResponse.class);
        when(taskTemplateMapper.toResponse(any(TaskTemplate.class))).thenReturn(response);

        TaskTemplateResponse result = taskTemplateService.createTemplate(1L, request);

        TaskTemplate saved = templateCaptor.getValue();
        assertThat(saved.getCreatorUser()).isEqualTo(user);
        assertThat(saved.getTitle()).isEqualTo("Template");
        assertThat(saved.getDescription()).isEqualTo("Desc");
        assertThat(saved.isPublicTemplate()).isTrue();
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getTraits()).containsExactlyInAnyOrder(focus, discipline);

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("createTemplate debe fallar si usuario no existe")
    void createTemplate_shouldThrowWhenUserNotFound() {
        CreateTaskTemplateRequest request = mock(CreateTaskTemplateRequest.class);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskTemplateService.createTemplate(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createTemplate debe fallar si faltan traits activos")
    void createTemplate_shouldThrowWhenTraitsMissing() {
        CreateTaskTemplateRequest request = mock(CreateTaskTemplateRequest.class);
        when(request.getTraitCodes()).thenReturn(Set.of("FOCUS", "DISCIPLINE"));

        Trait focus = Trait.builder().id(1L).code("FOCUS").active(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(traitRepository.findByCodeInAndActiveTrue(Set.of("FOCUS", "DISCIPLINE")))
                .thenReturn(List.of(focus));

        assertThatThrownBy(() -> taskTemplateService.createTemplate(1L, request))
                .isInstanceOf(BusinessException.class);

        verify(taskTemplateRepository, never()).save(any());
    }

    @Test
    @DisplayName("getMyActiveTemplates debe mapear plantillas activas del usuario")
    void getMyActiveTemplates_shouldReturnMappedTemplates() {
        TaskTemplate template1 = TaskTemplate.builder().id(1L).build();
        TaskTemplate template2 = TaskTemplate.builder().id(2L).build();

        TaskTemplateResponse response1 = mock(TaskTemplateResponse.class);
        TaskTemplateResponse response2 = mock(TaskTemplateResponse.class);

        when(taskTemplateRepository.findByCreatorUserIdAndActiveTrue(1L)).thenReturn(List.of(template1, template2));
        when(taskTemplateMapper.toResponse(template1)).thenReturn(response1);
        when(taskTemplateMapper.toResponse(template2)).thenReturn(response2);

        List<TaskTemplateResponse> result = taskTemplateService.getMyActiveTemplates(1L);

        assertThat(result).containsExactly(response1, response2);
    }

    @Test
    @DisplayName("getPublicActiveTemplates debe mapear plantillas públicas activas")
    void getPublicActiveTemplates_shouldReturnMappedTemplates() {
        TaskTemplate template1 = TaskTemplate.builder().id(1L).build();
        TaskTemplate template2 = TaskTemplate.builder().id(2L).build();

        TaskTemplateResponse response1 = mock(TaskTemplateResponse.class);
        TaskTemplateResponse response2 = mock(TaskTemplateResponse.class);

        when(taskTemplateRepository.findByPublicTemplateTrueAndActiveTrue()).thenReturn(List.of(template1, template2));
        when(taskTemplateMapper.toResponse(template1)).thenReturn(response1);
        when(taskTemplateMapper.toResponse(template2)).thenReturn(response2);

        List<TaskTemplateResponse> result = taskTemplateService.getPublicActiveTemplates();

        assertThat(result).containsExactly(response1, response2);
    }

    @Test
    @DisplayName("getMyTemplateById debe devolver plantilla propia activa")
    void getMyTemplateById_shouldReturnMappedTemplate() {
        TaskTemplate template = TaskTemplate.builder().id(10L).build();
        TaskTemplateResponse response = mock(TaskTemplateResponse.class);

        when(taskTemplateRepository.findByIdAndCreatorUserIdAndActiveTrue(10L, 1L)).thenReturn(Optional.of(template));
        when(taskTemplateMapper.toResponse(template)).thenReturn(response);

        TaskTemplateResponse result = taskTemplateService.getMyTemplateById(1L, 10L);

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("getMyTemplateById debe fallar si no existe")
    void getMyTemplateById_shouldThrowWhenNotFound() {
        when(taskTemplateRepository.findByIdAndCreatorUserIdAndActiveTrue(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskTemplateService.getMyTemplateById(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getPublicTemplateById debe devolver plantilla pública activa")
    void getPublicTemplateById_shouldReturnMappedTemplate() {
        TaskTemplate template = TaskTemplate.builder().id(10L).build();
        TaskTemplateResponse response = mock(TaskTemplateResponse.class);

        when(taskTemplateRepository.findByIdAndPublicTemplateTrueAndActiveTrue(10L)).thenReturn(Optional.of(template));
        when(taskTemplateMapper.toResponse(template)).thenReturn(response);

        TaskTemplateResponse result = taskTemplateService.getPublicTemplateById(10L);

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("getPublicTemplateById debe fallar si no existe")
    void getPublicTemplateById_shouldThrowWhenNotFound() {
        when(taskTemplateRepository.findByIdAndPublicTemplateTrueAndActiveTrue(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskTemplateService.getPublicTemplateById(10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
