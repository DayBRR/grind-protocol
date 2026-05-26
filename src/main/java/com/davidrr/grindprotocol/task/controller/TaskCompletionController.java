package com.davidrr.grindprotocol.task.controller;

import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import com.davidrr.grindprotocol.task.dto.CreateTaskCompletionRequest;
import com.davidrr.grindprotocol.task.dto.TaskCompletionResponse;
import com.davidrr.grindprotocol.task.service.TaskCompletionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me/task-completions")
@RequiredArgsConstructor
@Tag(name = "Task Completions", description = "Completados de tareas del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class TaskCompletionController {

    private final TaskCompletionService taskCompletionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Completar una tarea",
            description = "Registra la completion de una tarea del usuario autenticado y aplica XP, Core Points, daily progress y streak."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarea completada correctamente"),
            @ApiResponse(responseCode = "400", description = "Completion no permitida o datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    public TaskCompletionResponse completeTask(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateTaskCompletionRequest request
    ) {
        return taskCompletionService.completeTask(
                currentUser.getId(),
                request
        );
    }

    @GetMapping("/today")
    @Operation(
            summary = "Listar completions de hoy",
            description = "Devuelve las tareas completadas por el usuario autenticado en la fecha actual."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Completions de hoy obtenidas correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public List<TaskCompletionResponse> getTodayCompletions(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return taskCompletionService.getTodayCompletions(currentUser.getId());
    }
}
