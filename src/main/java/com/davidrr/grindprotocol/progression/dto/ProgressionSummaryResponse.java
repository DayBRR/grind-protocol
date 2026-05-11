package com.davidrr.grindprotocol.progression.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Resumen de progreso del usuario autenticado")
public class ProgressionSummaryResponse {

    @Schema(description = "Experiencia total acumulada", example = "250")
    private Long totalXp;

    @Schema(description = "Nivel actual del usuario", example = "3")
    private Integer level;

    @Schema(description = "XP mínima necesaria para el nivel actual", example = "200")
    private Long xpForCurrentLevel;

    @Schema(description = "XP necesaria para alcanzar el siguiente nivel", example = "300")
    private Long xpForNextLevel;

    @Schema(description = "XP acumulada dentro del nivel actual", example = "50")
    private Long xpProgressInCurrentLevel;

    @Schema(description = "XP restante para alcanzar el siguiente nivel", example = "50")
    private Long xpRemainingForNextLevel;

    @Schema(description = "Core Points acumulados", example = "25")
    private Long corePoints;

    @Schema(description = "Racha actual del usuario", example = "4")
    private Integer currentStreak;

    @Schema(description = "Mejor racha histórica del usuario", example = "7")
    private Integer bestStreak;
}