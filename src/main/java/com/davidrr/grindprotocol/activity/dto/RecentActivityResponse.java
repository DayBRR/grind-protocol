package com.davidrr.grindprotocol.activity.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RecentActivityResponse(
        List<ActivityItemResponse> items
) {
}
