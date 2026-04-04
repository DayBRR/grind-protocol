package com.davidrr.grindprotocol.userprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Perfil del usuario")
public class UserProfileResponse {

    @Schema(description = "Identificador del usuario", example = "1")
    private Long userId;

    @Schema(description = "Nombre de usuario", example = "david")
    private String username;

    @Schema(description = "Nombre visible del perfil", example = "David Ruiz")
    private String displayName;

    @Schema(description = "Objetivo diario de tareas", example = "5")
    private Integer dailyTaskGoal;

    @Schema(description = "Experiencia total acumulada", example = "0")
    private Long totalXp;

    @Schema(description = "Puntos de la aplicación", example = "0")
    private Long corePoints;

    @Schema(description = "Racha actual", example = "0")
    private Integer currentStreak;

    @Schema(description = "Mejor racha histórica", example = "0")
    private Integer bestStreak;

    @Schema(description = "Última fecha evaluada del progreso", example = "2026-04-04", nullable = true)
    private LocalDate lastEvaluatedDate;
}