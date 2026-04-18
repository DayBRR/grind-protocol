package com.davidrr.grindprotocol.task.service;

import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.task.dto.CreateTaskCompletionRequest;
import com.davidrr.grindprotocol.task.dto.TaskCompletionResponse;
import com.davidrr.grindprotocol.task.enums.CompletionSource;
import com.davidrr.grindprotocol.task.mapper.TaskCompletionMapper;
import com.davidrr.grindprotocol.task.model.Task;
import com.davidrr.grindprotocol.task.model.TaskCompletion;
import com.davidrr.grindprotocol.task.repository.TaskCompletionRepository;
import com.davidrr.grindprotocol.task.repository.TaskRepository;
import com.davidrr.grindprotocol.task.service.impl.TaskCompletionServiceImpl;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskCompletionServiceImplTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskCompletionRepository taskCompletionRepository;
    @Mock
    private DailyProgressService dailyProgressService;
    @Mock
    private TaskCompletionMapper taskCompletionMapper;

    @InjectMocks
    private TaskCompletionServiceImpl taskCompletionService;

    private User user;
    private Task task;

    @BeforeEach
    void setUp() {
        user = TestDataFactory.user();
        task = TestDataFactory.task(10L, user, true);
        task.setBaseXp(20);
        task.setStreakEligible(true);
        task.setRepeatable(false);
        task.setMaxCompletionsPerDay(1);
        task.setDiminishingReturnsEnabled(false);
        task.setActive(true);
    }

    @Test
    @DisplayName("completeTask debe crear el primer completado del día y recalcular DailyProgress")
    void completeTask_shouldCreateFirstCompletionAndRecalculateProgress() {
        CreateTaskCompletionRequest request = TestDataFactory.createTaskCompletionRequest(10L);
        request.setNotes("primer completion");

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskCompletionRepository.findTopByTaskIdAndCompletionDateOrderByCompletionIndexForDayDesc(eq(10L), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        ArgumentCaptor<TaskCompletion> completionCaptor = ArgumentCaptor.forClass(TaskCompletion.class);

        when(taskCompletionRepository.save(completionCaptor.capture())).thenAnswer(invocation -> {
            TaskCompletion completion = invocation.getArgument(0);
            completion.setId(100L);
            return completion;
        });

        TaskCompletionResponse response = mock(TaskCompletionResponse.class);
        when(taskCompletionMapper.toResponse(any(TaskCompletion.class))).thenReturn(response);

        TaskCompletionResponse result = taskCompletionService.completeTask(1L, request);

        TaskCompletion saved = completionCaptor.getValue();
        assertThat(saved.getTask()).isEqualTo(task);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getCompletionIndexForDay()).isEqualTo(1);
        assertThat(saved.isCountedForDailyGoal()).isTrue();
        assertThat(saved.isCountedForStreak()).isTrue();
        assertThat(saved.getBaseXp()).isEqualTo(20);
        assertThat(saved.getAwardedXp()).isEqualTo(20);
        assertThat(saved.getAwardedCorePoints()).isEqualTo(2);
        assertThat(saved.getNotes()).isEqualTo("primer completion");
        assertThat(saved.getSource()).isEqualTo(CompletionSource.MANUAL);

        verify(dailyProgressService).recalculateDailyProgress(eq(1L), any(LocalDate.class));
        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("completeTask debe aplicar diminishing returns en repeticiones")
    void completeTask_shouldApplyDiminishingReturnsOnRepeat() {
        task.setRepeatable(true);
        task.setMaxCompletionsPerDay(3);
        task.setDiminishingReturnsEnabled(true);
        task.setBaseXp(21);

        CreateTaskCompletionRequest request = TestDataFactory.createTaskCompletionRequest(10L);
        request.setNotes("segunda");

        TaskCompletion previous = TestDataFactory.taskCompletion(99L, task, user, 1, true, true);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskCompletionRepository.findTopByTaskIdAndCompletionDateOrderByCompletionIndexForDayDesc(eq(10L), any(LocalDate.class)))
                .thenReturn(Optional.of(previous));

        ArgumentCaptor<TaskCompletion> completionCaptor = ArgumentCaptor.forClass(TaskCompletion.class);
        when(taskCompletionRepository.save(completionCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TaskCompletionResponse response = mock(TaskCompletionResponse.class);
        when(taskCompletionMapper.toResponse(any(TaskCompletion.class))).thenReturn(response);

        taskCompletionService.completeTask(1L, request);

        TaskCompletion saved = completionCaptor.getValue();
        assertThat(saved.getCompletionIndexForDay()).isEqualTo(2);
        assertThat(saved.isCountedForDailyGoal()).isFalse();
        assertThat(saved.isCountedForStreak()).isFalse();
        assertThat(saved.getAwardedXp()).isEqualTo(10);
        assertThat(saved.getAwardedCorePoints()).isEqualTo(1);
    }

    @Test
    @DisplayName("completeTask debe fallar si la tarea no existe")
    void completeTask_shouldThrowWhenTaskNotFound() {
        CreateTaskCompletionRequest request = TestDataFactory.createTaskCompletionRequest(999L);

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskCompletionService.completeTask(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskCompletionRepository, never()).save(any());
        verify(dailyProgressService, never()).recalculateDailyProgress(anyLong(), any());
    }

    @Test
    @DisplayName("completeTask debe fallar si la tarea pertenece a otro usuario")
    void completeTask_shouldThrowWhenTaskBelongsToAnotherUser() {
        User anotherUser = TestDataFactory.user(2L, "other", "other@test.com");
        task.setUser(anotherUser);

        CreateTaskCompletionRequest request = TestDataFactory.createTaskCompletionRequest(10L);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskCompletionService.completeTask(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskCompletionRepository, never()).save(any());
    }

    @Test
    @DisplayName("completeTask debe fallar si la tarea está inactiva")
    void completeTask_shouldThrowWhenTaskInactive() {
        task.setActive(false);

        CreateTaskCompletionRequest request = TestDataFactory.createTaskCompletionRequest(10L);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskCompletionService.completeTask(1L, request))
                .isInstanceOf(BusinessException.class);

        verify(taskCompletionRepository, never()).save(any());
    }

    @Test
    @DisplayName("completeTask debe fallar si la tarea no es repetible y ya se completó hoy")
    void completeTask_shouldThrowWhenNonRepeatableTaskAlreadyCompletedToday() {
        CreateTaskCompletionRequest request = TestDataFactory.createTaskCompletionRequest(10L);

        TaskCompletion previous = TestDataFactory.taskCompletion(99L, task, user, 1, true, true);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskCompletionRepository.findTopByTaskIdAndCompletionDateOrderByCompletionIndexForDayDesc(eq(10L), any(LocalDate.class)))
                .thenReturn(Optional.of(previous));

        assertThatThrownBy(() -> taskCompletionService.completeTask(1L, request))
                .isInstanceOf(BusinessException.class);

        verify(taskCompletionRepository, never()).save(any());
    }

    @Test
    @DisplayName("completeTask debe fallar si supera maxCompletionsPerDay")
    void completeTask_shouldThrowWhenMaxCompletionsExceeded() {
        task.setRepeatable(true);
        task.setMaxCompletionsPerDay(1);

        CreateTaskCompletionRequest request = TestDataFactory.createTaskCompletionRequest(10L);

        TaskCompletion previous = TestDataFactory.taskCompletion(99L, task, user, 1, true, true);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskCompletionRepository.findTopByTaskIdAndCompletionDateOrderByCompletionIndexForDayDesc(eq(10L), any(LocalDate.class)))
                .thenReturn(Optional.of(previous));

        assertThatThrownBy(() -> taskCompletionService.completeTask(1L, request))
                .isInstanceOf(BusinessException.class);

        verify(taskCompletionRepository, never()).save(any());
    }

    @Test
    @DisplayName("getTodayCompletions debe mapear la lista de completados del día")
    void getTodayCompletions_shouldReturnMappedResponses() {
        TaskCompletion completion1 = TestDataFactory.taskCompletion(1L, task, user);
        TaskCompletion completion2 = TestDataFactory.taskCompletion(2L, task, user);

        TaskCompletionResponse response1 = mock(TaskCompletionResponse.class);
        TaskCompletionResponse response2 = mock(TaskCompletionResponse.class);

        when(taskCompletionRepository.findByUserIdAndCompletionDate(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(completion1, completion2));
        when(taskCompletionMapper.toResponse(completion1)).thenReturn(response1);
        when(taskCompletionMapper.toResponse(completion2)).thenReturn(response2);

        List<TaskCompletionResponse> result = taskCompletionService.getTodayCompletions(1L);

        assertThat(result).containsExactly(response1, response2);
    }
}