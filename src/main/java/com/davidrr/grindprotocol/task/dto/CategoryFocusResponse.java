package com.davidrr.grindprotocol.task.dto;

import com.davidrr.grindprotocol.task.enums.CategoryFocusPeriod;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record CategoryFocusResponse(
        CategoryFocusPeriod period,
        LocalDate startDate,
        LocalDate endDate,
        List<CategoryFocusItemResponse> categories
) {
}
