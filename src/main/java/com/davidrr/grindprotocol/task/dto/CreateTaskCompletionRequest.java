package com.davidrr.grindprotocol.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskCompletionRequest {

    @NotNull(message = "taskId is required")
    private Long taskId;

    private String notes;
}