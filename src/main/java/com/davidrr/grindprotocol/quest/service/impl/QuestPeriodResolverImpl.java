package com.davidrr.grindprotocol.quest.service.impl;

import com.davidrr.grindprotocol.quest.enums.QuestFrequency;
import com.davidrr.grindprotocol.quest.service.QuestPeriodResolver;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Component
public class QuestPeriodResolverImpl implements QuestPeriodResolver {

    @Override
    public LocalDate resolvePeriodStart(
            QuestFrequency frequency,
            LocalDate referenceDate
    ) {
        return switch (frequency) {
            case DAILY -> referenceDate;

            case WEEKLY -> referenceDate.with(
                    TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
            );

            case SPECIAL -> LocalDate.of(1970, 1, 1);
        };
    }

    @Override
    public LocalDate resolvePeriodEnd(
            QuestFrequency frequency,
            LocalDate referenceDate
    ) {
        return switch (frequency) {
            case DAILY -> referenceDate;

            case WEEKLY -> referenceDate.with(
                    TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)
            );

            case SPECIAL -> LocalDate.of(9999, 12, 31);
        };
    }
}