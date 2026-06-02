package com.davidrr.grindprotocol.quest.service;

import com.davidrr.grindprotocol.quest.enums.QuestFrequency;

import java.time.LocalDate;

public interface QuestPeriodResolver {

    LocalDate resolvePeriodStart(QuestFrequency frequency, LocalDate referenceDate);

    LocalDate resolvePeriodEnd(QuestFrequency frequency, LocalDate referenceDate);
}