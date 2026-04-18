package com.davidrr.grindprotocol.task.dto;

import com.davidrr.grindprotocol.task.enums.CompletionSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCompletionResponse {

    private Long id;
    private Long taskId;
    private Long userId;

    private LocalDateTime completedAt;
    private LocalDate completionDate;
    private Integer completionIndexForDay;

    private boolean countedForDailyGoal;
    private boolean countedForStreak;

    private Integer baseXp;
    private Integer awardedXp;
    private Integer awardedCorePoints;

    private String notes;
    private CompletionSource source;
}