package com.davidrr.grindprotocol.userprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Schema(description = "Datos editables del perfil del usuario autenticado")
public class UpdateUserProfileRequest {

    @NotBlank
    @Schema(
            description = "Nombre visible del usuario",
            example = "David Ruiz"
    )
    private String displayName;

    @Min(1)
    @Max(10)
    @Schema(
            description = "Objetivo diario de tareas",
            example = "5",
            minimum = "1",
            maximum = "10"
    )
    private Integer dailyTaskGoal;
}