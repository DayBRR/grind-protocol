package com.davidrr.grindprotocol.task.controller;

import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import com.davidrr.grindprotocol.task.dto.DailyProgressResponse;
import com.davidrr.grindprotocol.task.service.DailyProgressService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/daily-progress")
@RequiredArgsConstructor
@Tag(name = "Daily Progress", description = "Progreso diario del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class DailyProgressController {

    private final DailyProgressService dailyProgressService;

    @GetMapping("/today")
    @Operation(
            summary = "Obtener progreso diario de hoy",
            description = "Devuelve el progreso diario del usuario autenticado para la fecha actual."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progreso diario obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Perfil de usuario no encontrado")
    })
    public DailyProgressResponse getTodayProgress(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return dailyProgressService.getTodayProgress(currentUser.getId());
    }

}
