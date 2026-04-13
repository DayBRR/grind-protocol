package com.davidrr.grindprotocol.task.service.impl;

import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.common.exception.ErrorMessages;
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
import com.davidrr.grindprotocol.task.service.TaskService;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskTemplateRepository taskTemplateRepository;
    private final TraitRepository traitRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskResponse createTask(Long userId, CreateTaskRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.User.NOT_FOUND,
                        ErrorMessages.User.NOT_FOUND
                ));

        validateCreateTaskRequest(request);

        Set<String> normalizedTraitCodes = normalizeTraitCodes(request.getTraitCodes());
        Set<Trait> traits = resolveTraits(normalizedTraitCodes);

        Task task = Task.builder()
                .user(user)
                .template(null)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .taskType(request.getTaskType())
                .baseXp(request.getBaseXp())
                .mandatory(request.isMandatory())
                .streakEligible(request.isStreakEligible())
                .repeatable(request.isRepeatable())
                .maxCompletionsPerDay(request.getMaxCompletionsPerDay())
                .diminishingReturnsEnabled(request.isDiminishingReturnsEnabled())
                .active(request.isActive())
                .dueTime(request.getDueTime())
                .weeklyClosingDay(request.getWeeklyClosingDay())
                .traits(traits)
                .build();

        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }

    @Override
    @Transactional
    public TaskResponse createTaskFromTemplate(Long userId, CreateTaskFromTemplateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.User.NOT_FOUND,
                        ErrorMessages.User.NOT_FOUND
                ));

        TaskTemplate template = taskTemplateRepository.findByIdAndActiveTrue(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.Task.NOT_FOUND,
                        "Plantilla no encontrada"
                ));

        validateTemplateAccess(userId, template);

        Task task = Task.builder()
                .user(user)
                .template(template)
                .title(resolveTitle(request, template))
                .description(resolveDescription(request, template))
                .category(template.getCategory())
                .difficulty(template.getDifficulty())
                .taskType(template.getTaskType())
                .baseXp(template.getBaseXp())
                .mandatory(template.isMandatory())
                .streakEligible(template.isStreakEligible())
                .repeatable(template.isRepeatable())
                .maxCompletionsPerDay(template.getMaxCompletionsPerDay())
                .diminishingReturnsEnabled(template.isDiminishingReturnsEnabled())
                .active(template.isActive())
                .dueTime(resolveDueTime(request, template))
                .weeklyClosingDay(resolveWeeklyClosingDay(request, template))
                .traits(copyTraits(template.getTraits()))
                .build();

        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }

    @Override
    public List<TaskResponse> getActiveTasksByUser(Long userId) {
        return taskRepository.findWithTraitsByUserIdAndActiveTrue(userId)
                .stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    public TaskResponse getTaskById(Long userId, Long taskId) {
        Task task = taskRepository.findWithTraitsByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.Task.NOT_FOUND,
                        ErrorMessages.Task.NOT_FOUND
                ));

        return taskMapper.toResponse(task);
    }

    private void validateCreateTaskRequest(CreateTaskRequest request) {
        if (!request.isRepeatable() && request.getMaxCompletionsPerDay() > 1) {
            throw new BusinessException(
                    ErrorCodes.Task.CONFIGURATION_INVALID,
                    "Una tarea no repetible no puede tener maxCompletionsPerDay mayor que 1"
            );
        }
    }

    private void validateTemplateAccess(Long userId, TaskTemplate template) {
        boolean isOwner = template.getCreatorUser() != null
                && template.getCreatorUser().getId().equals(userId);

        if (!isOwner && !template.isPublicTemplate()) {
            throw new ResourceNotFoundException(
                    ErrorCodes.Task.NOT_FOUND,
                    "Plantilla no encontrada"
            );
        }
    }

    private String resolveTitle(CreateTaskFromTemplateRequest request, TaskTemplate template) {
        if (request.getTitleOverride() != null && !request.getTitleOverride().isBlank()) {
            return request.getTitleOverride().trim();
        }
        return template.getTitle();
    }

    private String resolveDescription(CreateTaskFromTemplateRequest request, TaskTemplate template) {
        if (request.getDescriptionOverride() != null && !request.getDescriptionOverride().isBlank()) {
            return request.getDescriptionOverride().trim();
        }
        return template.getDescription();
    }

    private java.time.LocalTime resolveDueTime(CreateTaskFromTemplateRequest request, TaskTemplate template) {
        if (request.getDueTimeOverride() != null) {
            return request.getDueTimeOverride();
        }
        return template.getDueTime();
    }

    private Integer resolveWeeklyClosingDay(CreateTaskFromTemplateRequest request, TaskTemplate template) {
        if (request.getWeeklyClosingDayOverride() != null) {
            if (request.getWeeklyClosingDayOverride() < 1 || request.getWeeklyClosingDayOverride() > 7) {
                throw new BusinessException(
                        ErrorCodes.Task.CONFIGURATION_INVALID,
                        "weeklyClosingDayOverride debe estar entre 1 y 7"
                );
            }
            return request.getWeeklyClosingDayOverride();
        }
        return template.getWeeklyClosingDay();
    }

    private Set<Trait> copyTraits(Set<Trait> traits) {
        if (traits == null || traits.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(traits);
    }

    private Set<String> normalizeTraitCodes(Set<String> traitCodes) {
        if (traitCodes == null || traitCodes.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return traitCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(code -> code.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Trait> resolveTraits(Set<String> traitCodes) {
        if (traitCodes.isEmpty()) {
            return new LinkedHashSet<>();
        }

        List<Trait> traits = traitRepository.findByCodeInAndActiveTrue(traitCodes);

        if (traits.size() != traitCodes.size()) {
            Set<String> foundCodes = traits.stream()
                    .map(Trait::getCode)
                    .collect(Collectors.toSet());

            Set<String> missingCodes = new LinkedHashSet<>(traitCodes);
            missingCodes.removeAll(foundCodes);

            throw new BusinessException(
                    ErrorCodes.Task.TRAIT_NOT_FOUND_OR_INACTIVE,
                    "Traits inválidos o inactivos: " + missingCodes
            );
        }

        return new LinkedHashSet<>(traits);
    }
}