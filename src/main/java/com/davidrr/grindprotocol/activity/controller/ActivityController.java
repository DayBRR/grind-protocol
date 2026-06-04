package com.davidrr.grindprotocol.activity.controller;

import com.davidrr.grindprotocol.activity.dto.RecentActivityResponse;
import com.davidrr.grindprotocol.activity.service.UserActivityEventService;
import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/activity")
@RequiredArgsConstructor
@Tag(name = "Activity", description = "Endpoints de actividad reciente del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class ActivityController {

    private final UserActivityEventService userActivityEventService;

    @GetMapping("/recent")
    @Operation(
            summary = "Obtener actividad reciente",
            description = "Devuelve los últimos eventos de actividad generados por tareas, quests, achievements y recompensas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actividad reciente obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public RecentActivityResponse getRecentActivity(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser,

            @Parameter(description = "Número máximo de eventos a devolver. Máximo 50.", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        return userActivityEventService.getRecentActivity(currentUser.getId(), limit);
    }
}
