package com.davidrr.grindprotocol.activity.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record DailyProgressionSummaryResponse(
        LocalDate date,
        String dayOfWeek,
        Integer xpEarned,
        Integer corePointsEarned,
        Integer taskCompletions,
        Integer questClaims,
        Integer achievementClaims,
        Integer rewardRedemptions
) {
}
