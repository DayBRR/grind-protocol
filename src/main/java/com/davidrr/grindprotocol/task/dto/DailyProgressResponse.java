package com.davidrr.grindprotocol.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyProgressResponse {

    private Long id;
    private Long userId;
    private LocalDate progressDate;

    private Integer requiredTaskCount;
    private Integer completedValidTaskCount;

    private Integer mandatoryTasksRequired;
    private Integer mandatoryTasksCompleted;

    private boolean dayQualified;

    private LocalDateTime evaluatedAt;
}