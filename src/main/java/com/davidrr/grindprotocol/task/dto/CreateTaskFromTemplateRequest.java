package com.davidrr.grindprotocol.task.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskFromTemplateRequest {

    @NotNull(message = "El campo 'templateId' es obligatorio")
    private Long templateId;

    @Size(max = 150, message = "El campo 'titleOverride' no puede superar 150 caracteres")
    private String titleOverride;

    @Size(max = 1000, message = "El campo 'descriptionOverride' no puede superar 1000 caracteres")
    private String descriptionOverride;

    private LocalTime dueTimeOverride;

    private Integer weeklyClosingDayOverride;
}