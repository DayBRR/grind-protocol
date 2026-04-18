package com.davidrr.grindprotocol.task.service;

import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.task.dto.CreateTaskFromTemplateRequest;
import com.davidrr.grindprotocol.task.dto.CreateTaskRequest;
import com.davidrr.grindprotocol.task.dto.TaskResponse;
import com.davidrr.grindprotocol.task.mapper.TaskMapper;
import com.davidrr.grindprotocol.task.model.Task;
import com.davidrr.grindprotocol.task.model.TaskTemplate;
import com.davidrr.grindprotocol.task.model.Trait;
import com.davidrr.grindprotocol.task.repository.TaskRepository;
import com.davidrr.grindprotocol.task.repository.TaskTemplateRepository;
import com.davidrr.grindprotocol.task.repository.TraitRepository;
import com.davidrr.grindprotocol.task.service.impl.TaskServiceImpl;
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

import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock private TaskRepository taskRepository;
    @Mock private TaskTemplateRepository taskTemplateRepository;
    @Mock private TraitRepository traitRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User user;

    @BeforeEach
    void setUp() {
        user = TestDataFactory.user();
    }

    @Test
    @DisplayName("createTask debe crear una tarea manual con traits normalizados")
    void createTask_shouldCreateManualTaskWithNormalizedTraits() {
        CreateTaskRequest request = TestDataFactory.createTaskRequest();
        request.setTitle("  Study Spring  ");
        request.setDescription("JWT");
        request.setTraitCodes(Set.of(" focus ", "discipline"));

        Trait focus = TestDataFactory.trait(1L, "FOCUS", "Focus");
        Trait discipline = TestDataFactory.trait(2L, "DISCIPLINE", "Discipline");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(traitRepository.findByCodeInAndActiveTrue(Set.of("FOCUS", "DISCIPLINE")))
                .thenReturn(List.of(focus, discipline));

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        when(taskRepository.save(taskCaptor.capture())).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(100L);
            return task;
        });

        TaskResponse response = mock(TaskResponse.class);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(response);

        TaskResponse result = taskService.createTask(1L, request);

        Task saved = taskCaptor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getTemplate()).isNull();
        assertThat(saved.getTitle()).isEqualTo("Study Spring");
        assertThat(saved.getDescription()).isEqualTo("JWT");
        assertThat(saved.isMandatory()).isTrue();
        assertThat(saved.isRepeatable()).isFalse();
        assertThat(saved.getMaxCompletionsPerDay()).isEqualTo(1);
        assertThat(saved.getTraits()).containsExactlyInAnyOrder(focus, discipline);

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("createTask debe fallar si usuario no existe")
    void createTask_shouldThrowWhenUserNotFound() {
        CreateTaskRequest request = TestDataFactory.createTaskRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createTask debe fallar si una tarea no repetible tiene maxCompletionsPerDay mayor que 1")
    void createTask_shouldThrowWhenConfigurationInvalid() {
        CreateTaskRequest request = TestDataFactory.createTaskRequest();
        request.setRepeatable(false);
        request.setMaxCompletionsPerDay(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> taskService.createTask(1L, request))
                .isInstanceOf(BusinessException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTask debe fallar si faltan traits activos")
    void createTask_shouldThrowWhenTraitsMissingOrInactive() {
        CreateTaskRequest request = TestDataFactory.createTaskRequest();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(traitRepository.findByCodeInAndActiveTrue(anySet()))
                .thenReturn(List.of(TestDataFactory.trait(1L, "DISCIPLINE", "Discipline")));

        assertThatThrownBy(() -> taskService.createTask(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Traits inválidos o inactivos");

        verify(taskRepository, never()).save(any(Task.class));
        verify(taskMapper, never()).toResponse(any(Task.class));
    }

    @Test
    @DisplayName("createTaskFromTemplate debe crear tarea desde plantilla pública aplicando overrides")
    void createTaskFromTemplate_shouldCreateTaskFromPublicTemplateWithOverrides() {
        CreateTaskFromTemplateRequest request = TestDataFactory.createTaskFromTemplateRequest();
        request.setTemplateId(20L);
        request.setTitleOverride("  Override title ");
        request.setDescriptionOverride("  Override description ");
        request.setDueTimeOverride(LocalTime.of(18, 0));
        request.setWeeklyClosingDayOverride(5);

        TaskTemplate template = TestDataFactory.taskTemplate(user);
        template.setId(20L);
        template.setTitle("Template title");
        template.setDescription("Template description");
        template.setPublicTemplate(true);
        template.setDueTime(LocalTime.of(20, 0));
        template.setWeeklyClosingDay(7);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskTemplateRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(template));

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        when(taskRepository.save(taskCaptor.capture())).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(200L);
            return task;
        });

        TaskResponse response = mock(TaskResponse.class);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(response);

        TaskResponse result = taskService.createTaskFromTemplate(1L, request);

        Task saved = taskCaptor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getTemplate()).isEqualTo(template);
        assertThat(saved.getTitle()).isEqualTo("Override title");
        assertThat(saved.getDescription()).isEqualTo("Override description");
        assertThat(saved.getDueTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(saved.getWeeklyClosingDay()).isEqualTo(5);
        assertThat(saved.getTraits()).isNotSameAs(template.getTraits());
        assertThat(saved.getTraits()).containsExactlyElementsOf(template.getTraits());

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("createTaskFromTemplate debe permitir acceso a plantilla propia no pública")
    void createTaskFromTemplate_shouldAllowOwnPrivateTemplate() {
        CreateTaskFromTemplateRequest request = TestDataFactory.createTaskFromTemplateRequest();
        request.setTemplateId(20L);
        request.setTitleOverride(null);
        request.setDescriptionOverride(null);
        request.setDueTimeOverride(null);
        request.setWeeklyClosingDayOverride(null);

        TaskTemplate template = TestDataFactory.taskTemplate(user);
        template.setId(20L);
        template.setPublicTemplate(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskTemplateRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(template));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = mock(TaskResponse.class);
        when(taskMapper.toResponse(any(Task.class))).thenReturn(response);

        TaskResponse result = taskService.createTaskFromTemplate(1L, request);

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("createTaskFromTemplate debe fallar si plantilla privada pertenece a otro usuario")
    void createTaskFromTemplate_shouldThrowWhenTemplateIsPrivateAndNotOwned() {
        CreateTaskFromTemplateRequest request = TestDataFactory.createTaskFromTemplateRequest();
        request.setTemplateId(20L);

        User anotherUser = TestDataFactory.user(2L, "other", "other@test.com");
        TaskTemplate template = TestDataFactory.taskTemplate(anotherUser);
        template.setId(20L);
        template.setPublicTemplate(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskTemplateRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(template));

        assertThatThrownBy(() -> taskService.createTaskFromTemplate(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTaskFromTemplate debe fallar si weeklyClosingDayOverride no está entre 1 y 7")
    void createTaskFromTemplate_shouldThrowWhenWeeklyClosingDayOverrideInvalid() {
        CreateTaskFromTemplateRequest request = TestDataFactory.createTaskFromTemplateRequest();
        request.setTemplateId(20L);
        request.setWeeklyClosingDayOverride(9);

        TaskTemplate template = TestDataFactory.taskTemplate(user);
        template.setId(20L);
        template.setPublicTemplate(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskTemplateRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(template));

        assertThatThrownBy(() -> taskService.createTaskFromTemplate(1L, request))
                .isInstanceOf(BusinessException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("getActiveTasksByUser debe mapear las tareas activas")
    void getActiveTasksByUser_shouldReturnMappedTasks() {
        Task task1 = TestDataFactory.task(1L, user);
        Task task2 = TestDataFactory.task(2L, user);

        TaskResponse response1 = mock(TaskResponse.class);
        TaskResponse response2 = mock(TaskResponse.class);

        when(taskRepository.findWithTraitsByUserIdAndActiveTrue(1L)).thenReturn(List.of(task1, task2));
        when(taskMapper.toResponse(task1)).thenReturn(response1);
        when(taskMapper.toResponse(task2)).thenReturn(response2);

        List<TaskResponse> result = taskService.getActiveTasksByUser(1L);

        assertThat(result).containsExactly(response1, response2);
    }

    @Test
    @DisplayName("getTaskById debe devolver la tarea si pertenece al usuario")
    void getTaskById_shouldReturnMappedTask() {
        Task task = TestDataFactory.task(10L, user);
        TaskResponse response = mock(TaskResponse.class);

        when(taskRepository.findWithTraitsByIdAndUserId(10L, 1L)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(response);

        TaskResponse result = taskService.getTaskById(1L, 10L);

        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("getTaskById debe fallar si la tarea no existe o no pertenece al usuario")
    void getTaskById_shouldThrowWhenTaskNotFound() {
        when(taskRepository.findWithTraitsByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}