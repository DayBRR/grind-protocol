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
public class TaskResponse {

    private Long id;
    private Long userId;
    private Long templateId;

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
    private boolean active;

    private LocalTime dueTime;
    private Integer weeklyClosingDay;

    private Set<String> traitCodes;
}