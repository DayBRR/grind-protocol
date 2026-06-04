package com.davidrr.grindprotocol.activity.dto;

import com.davidrr.grindprotocol.activity.enums.ActivityType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record ActivityItemResponse(
        Long id,
        ActivityType type,
        String title,
        String description,
        Integer xpDelta,
        Integer corePointsDelta,
        LocalDateTime occurredAt,
        Map<String, Object> metadata
) {
}
