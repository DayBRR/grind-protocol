package com.davidrr.grindprotocol.progression.controller;

import com.davidrr.grindprotocol.progression.dto.ProgressionSummaryResponse;
import com.davidrr.grindprotocol.progression.service.ProgressionSummaryService;
import com.davidrr.grindprotocol.progression.service.StreakService;
import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/me/progression")
@RequiredArgsConstructor
@Tag(name = "Progression", description = "Endpoints para gestionar la progresión del usuario autenticado")
public class ProgressionController {

    private final StreakService streakService;
    private final ProgressionSummaryService progressionSummaryService;

    @PostMapping("/finalize-day")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Finalizar el progreso de un día",
            description = """
                    Evalúa el progreso diario del usuario autenticado para una fecha concreta y actualiza su racha.

                    Si no se informa la fecha, se utiliza la fecha actual.

                    Reglas principales:
                    - Si el día está calificado, se incrementa o inicia la racha.
                    - Si el día no está calificado, se reinicia la racha actual.
                    - Si la fecha ya fue evaluada, no se vuelve a modificar la racha.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Día finalizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Perfil de usuario o progreso diario no encontrado")
    })
    public void finalizeDay(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser,

            @Parameter(
                    description = "Fecha del día a finalizar. Si se omite, se usa la fecha actual.",
                    example = "2026-04-18"
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        streakService.finalizeDay(currentUser.getId(), targetDate);
    }

    @GetMapping
    @Operation(
            summary = "Obtener resumen de progresión",
            description = """
                    Devuelve el resumen de progresión del usuario autenticado.

                    Incluye:
                    - XP total acumulada.
                    - Nivel actual.
                    - XP mínima del nivel actual.
                    - XP necesaria para el siguiente nivel.
                    - XP acumulada dentro del nivel actual.
                    - XP restante para subir de nivel.
                    - Core Points.
                    - Racha actual.
                    - Mejor racha histórica.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen de progresión obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Perfil de usuario no encontrado")
    })
    public ProgressionSummaryResponse getSummary(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return progressionSummaryService.getSummary(currentUser.getId());
    }
}