package com.davidrr.grindprotocol.task.dto;

import com.davidrr.grindprotocol.task.enums.TaskCategory;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CategoryFocusItemResponse(
        TaskCategory category,
        Long completedTasks,
        Integer xpEarned,
        Integer corePointsEarned,
        BigDecimal percentage
) {
}
