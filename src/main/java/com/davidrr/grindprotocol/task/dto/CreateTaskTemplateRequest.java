package com.davidrr.grindprotocol.task.dto;

import com.davidrr.grindprotocol.common.validation.ValidationMessages;
import com.davidrr.grindprotocol.task.enums.TaskCategory;
import com.davidrr.grindprotocol.task.enums.TaskDifficulty;
import com.davidrr.grindprotocol.task.enums.TaskType;
import com.davidrr.grindprotocol.task.validation.ValidCreateTaskTemplateRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidCreateTaskTemplateRequest
public class CreateTaskTemplateRequest {

    @NotBlank(message = ValidationMessages.TaskTemplate.TITLE_OBLIGATORY)
    @Size(max = 150, message = ValidationMessages.TaskTemplate.TITLE_SIZE)
    private String title;

    @Size(max = 1000, message = ValidationMessages.TaskTemplate.DESCRIPTION_SIZE)
    private String description;

    @NotNull(message = ValidationMessages.TaskTemplate.CATEGORY_OBLIGATORY)
    private TaskCategory category;

    @NotNull(message = ValidationMessages.TaskTemplate.DIFFICULTY_OBLIGATORY)
    private TaskDifficulty difficulty;

    @NotNull(message = ValidationMessages.TaskTemplate.TASK_TYPE_OBLIGATORY)
    private TaskType taskType;

    @NotNull(message = ValidationMessages.TaskTemplate.BASE_XP_OBLIGATORY)
    @Min(value = 1, message = ValidationMessages.TaskTemplate.BASE_XP_MIN)
    private Integer baseXp;

    @Builder.Default
    private boolean mandatory = false;

    @Builder.Default
    private boolean streakEligible = true;

    @Builder.Default
    private boolean repeatable = false;

    @NotNull(message = ValidationMessages.TaskTemplate.MAX_COMPLETIONS_PER_DAY_OBLIGATORY)
    @Min(value = 1, message = ValidationMessages.TaskTemplate.MAX_COMPLETIONS_PER_DAY_MIN)
    @Max(value = 50, message = ValidationMessages.TaskTemplate.MAX_COMPLETIONS_PER_DAY_MAX)
    private Integer maxCompletionsPerDay;

    @Builder.Default
    private boolean diminishingReturnsEnabled = false;

    private LocalTime dueTime;

    @Min(value = 1, message = ValidationMessages.TaskTemplate.WEEKLY_CLOSING_DAY_IN_RANGE)
    @Max(value = 7, message = ValidationMessages.TaskTemplate.WEEKLY_CLOSING_DAY_IN_RANGE)
    private Integer weeklyClosingDay;

    @Builder.Default
    private boolean publicTemplate = false;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    @Size(max = 10, message = ValidationMessages.TaskTemplate.TRAIT_CODES_LIMIT)
    private Set<
            @NotBlank(message = ValidationMessages.TaskTemplate.TRAIT_CODES_EMPTY_VALUES)
            @Pattern(
                    regexp = ValidationMessages.TaskTemplate.TRAIT_CODE_PATTERN,
                    message = ValidationMessages.TaskTemplate.TRAIT_CODE_PATTERN_ERROR
            )
            String> traitCodes = new HashSet<>();
}