package com.davidrr.grindprotocol.progression.service;

import java.time.LocalDate;

public interface StreakService {
    void finalizeDay(Long userId, LocalDate date);
}