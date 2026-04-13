package com.davidrr.grindprotocol.task.dto;

import com.davidrr.grindprotocol.task.enums.TaskCategory;
import com.davidrr.grindprotocol.task.enums.TaskDifficulty;
import com.davidrr.grindprotocol.task.enums.TaskType;
import lombok.*;

import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskTemplateResponse {

    private Long id;
    private Long creatorUserId;

    private String title;
    private String description;

    private TaskCategory category;
    private TaskDifficulty difficulty;
    private TaskType taskType;

    private Integer baseXp;
    private boolean mandatory;
    private boolean streakEligible;
    private boolean repeatable;
    private Integer maxCompletionsPerDay;
    private boolean diminishingReturnsEnabled;

    private LocalTime dueTime;
    private Integer weeklyClosingDay;

    private boolean publicTemplate;
    private boolean active;

    private Set<String> traitCodes;
}