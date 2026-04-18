package com.davidrr.grindprotocol.task.service.impl;

import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.common.exception.ErrorMessages;
import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.task.dto.DailyProgressResponse;
import com.davidrr.grindprotocol.task.mapper.DailyProgressMapper;
import com.davidrr.grindprotocol.task.model.DailyProgress;
import com.davidrr.grindprotocol.task.model.Task;
import com.davidrr.grindprotocol.task.model.TaskCompletion;
import com.davidrr.grindprotocol.task.repository.DailyProgressRepository;
import com.davidrr.grindprotocol.task.repository.TaskCompletionRepository;
import com.davidrr.grindprotocol.task.repository.TaskRepository;
import com.davidrr.grindprotocol.task.service.DailyProgressService;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyProgressServiceImpl implements DailyProgressService {

    private static final int DEFAULT_DAILY_GOAL = 3;

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final DailyProgressRepository dailyProgressRepository;
    private final DailyProgressMapper dailyProgressMapper;

    @Override
    @Transactional
    public DailyProgressResponse recalculateDailyProgress(Long userId, LocalDate progressDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.User.NOT_FOUND,
                        ErrorMessages.User.NOT_FOUND
                ));

        List<Task> activeTasks = taskRepository.findWithTraitsByUserIdAndActiveTrue(userId);
        List<TaskCompletion> completions = taskCompletionRepository.findByUserIdAndCompletionDate(userId, progressDate);

        int requiredTaskCount = DEFAULT_DAILY_GOAL;

        int completedValidTaskCount = (int) completions.stream()
                .filter(TaskCompletion::isCountedForDailyGoal)
                .count();

        int mandatoryTasksRequired = (int) activeTasks.stream()
                .filter(Task::isMandatory)
                .count();

        Set<Long> completedMandatoryTaskIds = completions.stream()
                .filter(TaskCompletion::isCountedForDailyGoal)
                .map(tc -> tc.getTask().getId())
                .collect(Collectors.toSet());

        int mandatoryTasksCompleted = (int) activeTasks.stream()
                .filter(Task::isMandatory)
                .filter(task -> completedMandatoryTaskIds.contains(task.getId()))
                .count();

        boolean mandatoryCompleted =
                mandatoryTasksCompleted >= mandatoryTasksRequired;

        boolean dailyGoalReached =
                completedValidTaskCount >= requiredTaskCount;

        boolean dayQualified = mandatoryCompleted && dailyGoalReached;

        DailyProgress progress = dailyProgressRepository
                .findByUserIdAndProgressDate(userId, progressDate)
                .orElse(
                        DailyProgress.builder()
                                .user(user)
                                .progressDate(progressDate)
                                .build()
                );

        progress.setRequiredTaskCount(requiredTaskCount);
        progress.setCompletedValidTaskCount(completedValidTaskCount);
        progress.setMandatoryTasksRequired(mandatoryTasksRequired);
        progress.setMandatoryTasksCompleted(mandatoryTasksCompleted);
        progress.setDayQualified(dayQualified);
        progress.setEvaluatedAt(LocalDateTime.now());

        return dailyProgressMapper.toResponse(dailyProgressRepository.save(progress));
    }

    @Override
    public DailyProgressResponse getTodayProgress(Long userId) {
        return dailyProgressRepository.findByUserIdAndProgressDate(userId, LocalDate.now())
                .map(dailyProgressMapper::toResponse)
                .orElse(
                        DailyProgressResponse.builder()
                                .progressDate(LocalDate.now())
                                .requiredTaskCount(DEFAULT_DAILY_GOAL)
                                .completedValidTaskCount(0)
                                .mandatoryTasksRequired(0)
                                .mandatoryTasksCompleted(0)
                                .dayQualified(false)
                                .evaluatedAt(null)
                                .build()
                );
    }
}