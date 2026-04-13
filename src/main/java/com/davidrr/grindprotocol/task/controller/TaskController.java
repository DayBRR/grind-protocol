package com.davidrr.grindprotocol.task.controller;

import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import com.davidrr.grindprotocol.task.dto.CreateTaskFromTemplateRequest;
import com.davidrr.grindprotocol.task.dto.CreateTaskRequest;
import com.davidrr.grindprotocol.task.dto.TaskResponse;
import com.davidrr.grindprotocol.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
@RequestMapping("/me/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Endpoints para gestionar las tareas del usuario autenticado")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Crear una tarea manual",
            description = "Crea una tarea manual para el usuario autenticado con sus traits, categoría, dificultad y configuración de repetición."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Tarea creada correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "userId": 1,
                                      "templateId": null,
                                      "title": "Estudiar Spring Security",
                                      "description": "Repasar JWT, refresh tokens y tests",
                                      "category": "WORK",
                                      "difficulty": "HARD",
                                      "taskType": "DAILY",
                                      "baseXp": 120,
                                      "mandatory": true,
                                      "streakEligible": true,
                                      "repeatable": false,
                                      "maxCompletionsPerDay": 1,
                                      "diminishingReturnsEnabled": false,
                                      "active": true,
                                      "dueTime": null,
                                      "weeklyClosingDay": null,
                                      "traitCodes": ["TECH_SKILL", "FOCUS", "DISCIPLINE"]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public TaskResponse createTask(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos para crear una tarea manual",
                    content = @Content(
                            schema = @Schema(implementation = CreateTaskRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "title": "Estudiar Spring Security",
                                      "description": "Repasar JWT, refresh tokens y tests",
                                      "category": "WORK",
                                      "difficulty": "HARD",
                                      "taskType": "DAILY",
                                      "baseXp": 120,
                                      "mandatory": true,
                                      "streakEligible": true,
                                      "repeatable": false,
                                      "maxCompletionsPerDay": 1,
                                      "diminishingReturnsEnabled": false,
                                      "active": true,
                                      "traitCodes": ["TECH_SKILL", "FOCUS", "DISCIPLINE"]
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody CreateTaskRequest request
    ) {
        return taskService.createTask(currentUser.getId(), request);
    }

    @GetMapping
    @Operation(
            summary = "Listar mis tareas activas",
            description = "Devuelve todas las tareas activas del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de tareas obtenido correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public List<TaskResponse> getMyActiveTasks(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return taskService.getActiveTasksByUser(currentUser.getId());
    }

    @GetMapping("/{taskId}")
    @Operation(
            summary = "Obtener una tarea por id",
            description = "Devuelve el detalle de una tarea del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarea obtenida correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    public TaskResponse getTaskById(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long taskId
    ) {
        return taskService.getTaskById(currentUser.getId(), taskId);
    }

    @PostMapping("/from-template")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Crear una tarea desde plantilla",
            description = "Crea una tarea para el usuario autenticado a partir de una plantilla propia o pública."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Tarea creada correctamente desde plantilla",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Plantilla no encontrada")
    })
    public TaskResponse createTaskFromTemplate(
            @Parameter(hidden = true)
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateTaskFromTemplateRequest request
    ) {
        return taskService.createTaskFromTemplate(currentUser.getId(), request);
    }
}