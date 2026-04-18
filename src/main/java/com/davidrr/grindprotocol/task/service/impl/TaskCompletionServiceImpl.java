package com.davidrr.grindprotocol.task.service.impl;

import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.common.exception.ErrorMessages;
import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.task.dto.CreateTaskCompletionRequest;
import com.davidrr.grindprotocol.task.dto.TaskCompletionResponse;
import com.davidrr.grindprotocol.task.enums.CompletionSource;
import com.davidrr.grindprotocol.task.mapper.TaskCompletionMapper;
import com.davidrr.grindprotocol.task.model.Task;
import com.davidrr.grindprotocol.task.model.TaskCompletion;
import com.davidrr.grindprotocol.task.repository.TaskCompletionRepository;
import com.davidrr.grindprotocol.task.repository.TaskRepository;
import com.davidrr.grindprotocol.task.service.DailyProgressService;
import com.davidrr.grindprotocol.task.service.TaskCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskCompletionServiceImpl implements TaskCompletionService {

    private final TaskRepository taskRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final DailyProgressService dailyProgressService;
    private final TaskCompletionMapper taskCompletionMapper;

    @Override
    @Transactional
    public TaskCompletionResponse completeTask(Long userId, CreateTaskCompletionRequest request) {
        Long taskId = request.getTaskId();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.Task.NOT_FOUND,
                        ErrorMessages.Task.NOT_FOUND
                ));

        validateTaskOwnership(task, userId);
        validateTaskIsActive(task);

        LocalDate completionDate = LocalDate.now();
        int nextIndex = resolveNextCompletionIndex(taskId, completionDate);

        validateCompletionAllowed(task, nextIndex);

        int awardedXp = calculateAwardedXp(task, nextIndex);
        int awardedCorePoints = calculateAwardedCorePoints(awardedXp);

        TaskCompletion completion = TaskCompletion.builder()
                .task(task)
                .user(task.getUser())
                .completedAt(LocalDateTime.now())
                .completionDate(completionDate)
                .completionIndexForDay(nextIndex)
                .countedForDailyGoal(nextIndex == 1)
                .countedForStreak(nextIndex == 1 && task.isStreakEligible())
                .baseXp(task.getBaseXp())
                .awardedXp(awardedXp)
                .awardedCorePoints(awardedCorePoints)
                .notes(request.getNotes())
                .source(CompletionSource.MANUAL)
                .build();

        TaskCompletion saved = taskCompletionRepository.save(completion);

        dailyProgressService.recalculateDailyProgress(userId, completionDate);

        return taskCompletionMapper.toResponse(saved);
    }

    @Override
    public List<TaskCompletionResponse> getTodayCompletions(Long userId) {
        return taskCompletionRepository.findByUserIdAndCompletionDate(userId, LocalDate.now())
                .stream()
                .map(taskCompletionMapper::toResponse)
                .toList();
    }

    private void validateTaskOwnership(Task task, Long userId) {
        if (!task.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    ErrorCodes.Task.NOT_FOUND,
                    ErrorMessages.Task.NOT_FOUND
            );
        }
    }

    private void validateTaskIsActive(Task task) {
        if (!task.isActive()) {
            throw new BusinessException(
                    ErrorCodes.Task.COMPLETION_NOT_ALLOWED,
                    "No se puede completar una tarea inactiva"
            );
        }
    }

    private int resolveNextCompletionIndex(Long taskId, LocalDate completionDate) {
        return taskCompletionRepository
                .findTopByTaskIdAndCompletionDateOrderByCompletionIndexForDayDesc(taskId, completionDate)
                .map(last -> last.getCompletionIndexForDay() + 1)
                .orElse(1);
    }

    private void validateCompletionAllowed(Task task, int nextIndex) {
        if (!task.isRepeatable() && nextIndex > 1) {
            throw new BusinessException(
                    ErrorCodes.Task.COMPLETION_NOT_ALLOWED,
                    "La tarea no es repetible y ya fue completada hoy"
            );
        }

        if (nextIndex > task.getMaxCompletionsPerDay()) {
            throw new BusinessException(
                    ErrorCodes.Task.COMPLETION_NOT_ALLOWED,
                    "Se alcanzó el máximo de completados permitidos para hoy"
            );
        }
    }

    private int calculateAwardedXp(Task task, int completionIndex) {
        if (completionIndex == 1) {
            return task.getBaseXp();
        }

        if (task.isDiminishingReturnsEnabled()) {
            return Math.max(1, task.getBaseXp() / 2);
        }

        return task.getBaseXp();
    }

    private int calculateAwardedCorePoints(int awardedXp) {
        return awardedXp / 10;
    }
}
