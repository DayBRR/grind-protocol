package com.davidrr.grindprotocol.activity.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record WeeklyProgressionSummaryResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        Integer totalXp,
        Integer previousWeekTotalXp,
        BigDecimal deltaPercent,
        List<DailyProgressionSummaryResponse> days
) {
}
