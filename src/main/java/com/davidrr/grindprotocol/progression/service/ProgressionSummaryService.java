package com.davidrr.grindprotocol.progression.service;

import com.davidrr.grindprotocol.progression.dto.ProgressionSummaryResponse;

public interface ProgressionSummaryService {

    ProgressionSummaryResponse getSummary(Long userId);
}