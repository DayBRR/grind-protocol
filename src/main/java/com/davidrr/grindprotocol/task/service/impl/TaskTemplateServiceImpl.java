package com.davidrr.grindprotocol.task.service.impl;

import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.common.exception.ErrorMessages;
import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.task.dto.CreateTaskTemplateRequest;
import com.davidrr.grindprotocol.task.dto.TaskTemplateResponse;
import com.davidrr.grindprotocol.task.mapper.TaskTemplateMapper;
import com.davidrr.grindprotocol.task.model.TaskTemplate;
import com.davidrr.grindprotocol.task.model.Trait;
import com.davidrr.grindprotocol.task.repository.TaskTemplateRepository;
import com.davidrr.grindprotocol.task.repository.TraitRepository;
import com.davidrr.grindprotocol.task.service.TaskTemplateService;
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
public class TaskTemplateServiceImpl implements TaskTemplateService {

    private final TaskTemplateRepository taskTemplateRepository;
    private final TraitRepository traitRepository;
    private final UserRepository userRepository;
    private final TaskTemplateMapper taskTemplateMapper;

    @Override
    @Transactional
    public TaskTemplateResponse createTemplate(Long userId, CreateTaskTemplateRequest request) {
        User creatorUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.User.NOT_FOUND,
                        ErrorMessages.User.NOT_FOUND
                ));

        Set<String> normalizedTraitCodes = normalizeTraitCodes(request.getTraitCodes());
        Set<Trait> traits = resolveTraits(normalizedTraitCodes);

        TaskTemplate template = TaskTemplate.builder()
                .creatorUser(creatorUser)
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
                .dueTime(request.getDueTime())
                .weeklyClosingDay(request.getWeeklyClosingDay())
                .publicTemplate(request.isPublicTemplate())
                .active(request.isActive())
                .traits(traits)
                .build();

        TaskTemplate savedTemplate = taskTemplateRepository.save(template);
        return taskTemplateMapper.toResponse(savedTemplate);
    }

    @Override
    public List<TaskTemplateResponse> getMyActiveTemplates(Long userId) {
        return taskTemplateRepository.findByCreatorUserIdAndActiveTrue(userId)
                .stream()
                .map(taskTemplateMapper::toResponse)
                .toList();
    }

    @Override
    public List<TaskTemplateResponse> getPublicActiveTemplates() {
        return taskTemplateRepository.findByPublicTemplateTrueAndActiveTrue()
                .stream()
                .map(taskTemplateMapper::toResponse)
                .toList();
    }

    @Override
    public TaskTemplateResponse getMyTemplateById(Long userId, Long templateId) {
        TaskTemplate template = taskTemplateRepository.findByIdAndCreatorUserIdAndActiveTrue(templateId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.TaskTemplate.NOT_FOUND,
                        ErrorMessages.TaskTemplate.NOT_FOUND
                ));

        return taskTemplateMapper.toResponse(template);
    }

    @Override
    public TaskTemplateResponse getPublicTemplateById(Long templateId) {
        TaskTemplate template = taskTemplateRepository.findByIdAndPublicTemplateTrueAndActiveTrue(templateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.TaskTemplate.NOT_FOUND,
                        ErrorMessages.TaskTemplate.PUBLIC_NOT_FOUND
                ));

        return taskTemplateMapper.toResponse(template);
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
                    ErrorCodes.TaskTemplate.TRAIT_NOT_FOUND_OR_INACTIVE,
                    "Traits inválidos o inactivos: " + missingCodes
            );
        }

        return new LinkedHashSet<>(traits);
    }
}