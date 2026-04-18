package com.davidrr.grindprotocol.task.service;

import com.davidrr.grindprotocol.task.dto.DailyProgressResponse;
import com.davidrr.grindprotocol.task.mapper.DailyProgressMapper;
import com.davidrr.grindprotocol.task.model.DailyProgress;
import com.davidrr.grindprotocol.task.model.Task;
import com.davidrr.grindprotocol.task.model.TaskCompletion;
import com.davidrr.grindprotocol.task.repository.DailyProgressRepository;
import com.davidrr.grindprotocol.task.repository.TaskCompletionRepository;
import com.davidrr.grindprotocol.task.repository.TaskRepository;
import com.davidrr.grindprotocol.task.service.impl.DailyProgressServiceImpl;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyProgressServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCompletionRepository taskCompletionRepository;

    @Mock
    private DailyProgressRepository dailyProgressRepository;

    @Mock
    private DailyProgressMapper dailyProgressMapper;

    @InjectMocks
    private DailyProgressServiceImpl dailyProgressService;

    private User user;
    private LocalDate progressDate;

    @BeforeEach
    void setUp() {
        user = TestDataFactory.user();
        progressDate = LocalDate.of(2026, 4, 18);
    }

    @Test
    @DisplayName("Debe calificar el día cuando alcanza el mínimo y completa todas las obligatorias")
    void recalculateDailyProgress_shouldQualifyDay_whenGoalReachedAndMandatoryCompleted() {
        Task mandatoryTask1 = TestDataFactory.task(10L, user);
        Task mandatoryTask2 = TestDataFactory.task(11L, user);
        Task optionalTask = TestDataFactory.task(12L, user, false);

        TaskCompletion tc1 = TestDataFactory.taskCompletion(100L, mandatoryTask1, user);
        TaskCompletion tc2 = TestDataFactory.taskCompletion(101L, mandatoryTask2, user);
        TaskCompletion tc3 = TestDataFactory.taskCompletion(102L, optionalTask, user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.findWithTraitsByUserIdAndActiveTrue(1L))
                .thenReturn(List.of(mandatoryTask1, mandatoryTask2, optionalTask));
        when(taskCompletionRepository.findByUserIdAndCompletionDate(1L, progressDate))
                .thenReturn(List.of(tc1, tc2, tc3));
        when(dailyProgressRepository.findByUserIdAndProgressDate(1L, progressDate))
                .thenReturn(Optional.empty());

        ArgumentCaptor<DailyProgress> progressCaptor = ArgumentCaptor.forClass(DailyProgress.class);

        when(dailyProgressRepository.save(progressCaptor.capture()))
                .thenAnswer(invocation -> {
                    DailyProgress progress = invocation.getArgument(0);
                    progress.setId(999L);
                    return progress;
                });

        when(dailyProgressMapper.toResponse(any(DailyProgress.class)))
                .thenAnswer(invocation -> mapToResponse(invocation.getArgument(0)));

        DailyProgressResponse response = dailyProgressService.recalculateDailyProgress(1L, progressDate);

        DailyProgress savedProgress = progressCaptor.getValue();

        assertThat(savedProgress.getRequiredTaskCount()).isEqualTo(3);
        assertThat(savedProgress.getCompletedValidTaskCount()).isEqualTo(3);
        assertThat(savedProgress.getMandatoryTasksRequired()).isEqualTo(2);
        assertThat(savedProgress.getMandatoryTasksCompleted()).isEqualTo(2);
        assertThat(savedProgress.isDayQualified()).isTrue();
        assertThat(savedProgress.getEvaluatedAt()).isNotNull();

        assertThat(response.isDayQualified()).isTrue();
    }

    @Test
    @DisplayName("Debe calificar el día cuando supera el mínimo y supera también las obligatorias")
    void recalculateDailyProgress_shouldQualifyDay_whenCountsAreGreaterThanRequired() {
        Task mandatoryTask1 = TestDataFactory.task(10L, user);
        Task mandatoryTask2 = TestDataFactory.task(11L, user);
        Task optionalTask1 = TestDataFactory.task(12L, user,false);
        Task optionalTask2 = TestDataFactory.task(13L, user, false);

        TaskCompletion tc1 = TestDataFactory.taskCompletion(100L, mandatoryTask1, user,true);
        TaskCompletion tc2 = TestDataFactory.taskCompletion(101L, mandatoryTask2, user,true);
        TaskCompletion tc3 = TestDataFactory.taskCompletion(102L, optionalTask1, user,true);
        TaskCompletion tc4 = TestDataFactory.taskCompletion(103L, optionalTask2, user,true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.findWithTraitsByUserIdAndActiveTrue(1L))
                .thenReturn(List.of(mandatoryTask1, mandatoryTask2, optionalTask1, optionalTask2));
        when(taskCompletionRepository.findByUserIdAndCompletionDate(1L, progressDate))
                .thenReturn(List.of(tc1, tc2, tc3, tc4));
        when(dailyProgressRepository.findByUserIdAndProgressDate(1L, progressDate))
                .thenReturn(Optional.empty());

        ArgumentCaptor<DailyProgress> progressCaptor = ArgumentCaptor.forClass(DailyProgress.class);

        when(dailyProgressRepository.save(progressCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(dailyProgressMapper.toResponse(any(DailyProgress.class)))
                .thenAnswer(invocation -> mapToResponse(invocation.getArgument(0)));

        DailyProgressResponse response = dailyProgressService.recalculateDailyProgress(1L, progressDate);

        DailyProgress savedProgress = progressCaptor.getValue();

        assertThat(savedProgress.getCompletedValidTaskCount()).isEqualTo(4);
        assertThat(savedProgress.getMandatoryTasksRequired()).isEqualTo(2);
        assertThat(savedProgress.getMandatoryTasksCompleted()).isEqualTo(2);
        assertThat(savedProgress.isDayQualified()).isTrue();

        assertThat(response.isDayQualified()).isTrue();
    }

    @Test
    @DisplayName("No debe calificar el día cuando no alcanza el mínimo diario")
    void recalculateDailyProgress_shouldNotQualifyDay_whenDailyGoalNotReached() {
        Task mandatoryTask = TestDataFactory.task(10L, user,true);
        Task optionalTask = TestDataFactory.task(11L, user, false);

        TaskCompletion tc1 = TestDataFactory.taskCompletion(100L, mandatoryTask, user, true);
        TaskCompletion tc2 = TestDataFactory.taskCompletion(101L, optionalTask, user, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.findWithTraitsByUserIdAndActiveTrue(1L))
                .thenReturn(List.of(mandatoryTask, optionalTask));
        when(taskCompletionRepository.findByUserIdAndCompletionDate(1L, progressDate))
                .thenReturn(List.of(tc1, tc2));
        when(dailyProgressRepository.findByUserIdAndProgressDate(1L, progressDate))
                .thenReturn(Optional.empty());

        ArgumentCaptor<DailyProgress> progressCaptor = ArgumentCaptor.forClass(DailyProgress.class);

        when(dailyProgressRepository.save(progressCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(dailyProgressMapper.toResponse(any(DailyProgress.class)))
                .thenAnswer(invocation -> mapToResponse(invocation.getArgument(0)));

        DailyProgressResponse response = dailyProgressService.recalculateDailyProgress(1L, progressDate);

        DailyProgress savedProgress = progressCaptor.getValue();

        assertThat(savedProgress.getCompletedValidTaskCount()).isEqualTo(2);
        assertThat(savedProgress.isDayQualified()).isFalse();
        assertThat(response.isDayQualified()).isFalse();
    }

    @Test
    @DisplayName("No debe calificar el día cuando falta una obligatoria")
    void recalculateDailyProgress_shouldNotQualifyDay_whenMandatoryTaskMissing() {
        Task mandatoryTask1 = TestDataFactory.task(10L, user,true);
        Task mandatoryTask2 = TestDataFactory.task(11L, user,true);
        Task optionalTask = TestDataFactory.task(12L, user,false);

        TaskCompletion tc1 = TestDataFactory.taskCompletion(100L, mandatoryTask1, user, true);
        TaskCompletion tc2 = TestDataFactory.taskCompletion(101L, optionalTask, user, true);
        TaskCompletion tc3 = TestDataFactory.taskCompletion(102L, optionalTask, user, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.findWithTraitsByUserIdAndActiveTrue(1L))
                .thenReturn(List.of(mandatoryTask1, mandatoryTask2, optionalTask));
        when(taskCompletionRepository.findByUserIdAndCompletionDate(1L, progressDate))
                .thenReturn(List.of(tc1, tc2, tc3));
        when(dailyProgressRepository.findByUserIdAndProgressDate(1L, progressDate))
                .thenReturn(Optional.empty());

        ArgumentCaptor<DailyProgress> progressCaptor = ArgumentCaptor.forClass(DailyProgress.class);

        when(dailyProgressRepository.save(progressCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(dailyProgressMapper.toResponse(any(DailyProgress.class)))
                .thenAnswer(invocation -> mapToResponse(invocation.getArgument(0)));

        DailyProgressResponse response = dailyProgressService.recalculateDailyProgress(1L, progressDate);

        DailyProgress savedProgress = progressCaptor.getValue();

        assertThat(savedProgress.getCompletedValidTaskCount()).isEqualTo(3);
        assertThat(savedProgress.getMandatoryTasksRequired()).isEqualTo(2);
        assertThat(savedProgress.getMandatoryTasksCompleted()).isEqualTo(1);
        assertThat(savedProgress.isDayQualified()).isFalse();

        assertThat(response.isDayQualified()).isFalse();
    }

    @Test
    @DisplayName("Debe calificar el día cuando no hay tareas obligatorias y alcanza el mínimo")
    void recalculateDailyProgress_shouldQualifyDay_whenNoMandatoryTasksAndGoalReached() {
        Task optionalTask1 = TestDataFactory.task(10L, user,false);
        Task optionalTask2 = TestDataFactory.task(11L, user,false);
        Task optionalTask3 = TestDataFactory.task(12L, user,false);

        TaskCompletion tc1 = TestDataFactory.taskCompletion(100L, optionalTask1, user,true);
        TaskCompletion tc2 = TestDataFactory.taskCompletion(101L, optionalTask2, user,true);
        TaskCompletion tc3 = TestDataFactory.taskCompletion(102L, optionalTask3, user,true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.findWithTraitsByUserIdAndActiveTrue(1L))
                .thenReturn(List.of(optionalTask1, optionalTask2, optionalTask3));
        when(taskCompletionRepository.findByUserIdAndCompletionDate(1L, progressDate))
                .thenReturn(List.of(tc1, tc2, tc3));
        when(dailyProgressRepository.findByUserIdAndProgressDate(1L, progressDate))
                .thenReturn(Optional.empty());

        ArgumentCaptor<DailyProgress> progressCaptor = ArgumentCaptor.forClass(DailyProgress.class);

        when(dailyProgressRepository.save(progressCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(dailyProgressMapper.toResponse(any(DailyProgress.class)))
                .thenAnswer(invocation -> mapToResponse(invocation.getArgument(0)));

        DailyProgressResponse response = dailyProgressService.recalculateDailyProgress(1L, progressDate);

        DailyProgress savedProgress = progressCaptor.getValue();

        assertThat(savedProgress.getMandatoryTasksRequired()).isZero();
        assertThat(savedProgress.getMandatoryTasksCompleted()).isZero();
        assertThat(savedProgress.isDayQualified()).isTrue();

        assertThat(response.isDayQualified()).isTrue();
    }

    private DailyProgressResponse mapToResponse(DailyProgress progress) {
        return DailyProgressResponse.builder()
                .id(progress.getId())
                .userId(progress.getUser().getId())
                .progressDate(progress.getProgressDate())
                .requiredTaskCount(progress.getRequiredTaskCount())
                .completedValidTaskCount(progress.getCompletedValidTaskCount())
                .mandatoryTasksRequired(progress.getMandatoryTasksRequired())
                .mandatoryTasksCompleted(progress.getMandatoryTasksCompleted())
                .dayQualified(progress.isDayQualified())
                .evaluatedAt(progress.getEvaluatedAt())
                .build();
    }
}