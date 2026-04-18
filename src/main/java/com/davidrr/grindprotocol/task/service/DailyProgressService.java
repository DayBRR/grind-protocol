package com.davidrr.grindprotocol.task.service;

import com.davidrr.grindprotocol.task.dto.DailyProgressResponse;

import java.time.LocalDate;

public interface DailyProgressService {
    DailyProgressResponse recalculateDailyProgress(Long userId, LocalDate progressDate);
    DailyProgressResponse getTodayProgress(Long userId);
}