package com.davidrr.grindprotocol.task.controller;

import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import com.davidrr.grindprotocol.task.dto.CreateTaskTemplateRequest;
import com.davidrr.grindprotocol.task.dto.TaskTemplateResponse;
import com.davidrr.grindprotocol.task.service.TaskTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/me/task-templates")
@RequiredArgsConstructor
@Tag(name = "Task Templates", description = "Endpoints para gestionar plantillas de tareas")
public class TaskTemplateController {

    private final TaskTemplateService taskTemplateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Crear una plantilla de tarea",
            description = "Crea una plantilla de tarea para el usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Plantilla creada correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskTemplateResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public TaskTemplateResponse createTemplate(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateTaskTemplateRequest request
    ) {
        return taskTemplateService.createTemplate(currentUser.getId(), request);
    }

    @GetMapping
    @Operation(
            summary = "Listar mis plantillas activas",
            description = "Devuelve las plantillas activas creadas por el usuario autenticado."
    )
    public List<TaskTemplateResponse> getMyTemplates(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return taskTemplateService.getMyActiveTemplates(currentUser.getId());
    }

    @GetMapping("/{templateId}")
    @Operation(
            summary = "Obtener una plantilla propia por id",
            description = "Devuelve una plantilla activa del usuario autenticado."
    )
    public TaskTemplateResponse getMyTemplateById(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long templateId
    ) {
        return taskTemplateService.getMyTemplateById(currentUser.getId(), templateId);
    }
}