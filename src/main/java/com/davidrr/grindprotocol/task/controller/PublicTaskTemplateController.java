package com.davidrr.grindprotocol.task.controller;

import com.davidrr.grindprotocol.task.dto.TaskTemplateResponse;
import com.davidrr.grindprotocol.task.service.TaskTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task-templates/public")
@RequiredArgsConstructor
@Tag(name = "Public Task Templates", description = "Endpoints para consultar plantillas públicas")
public class PublicTaskTemplateController {

    private final TaskTemplateService taskTemplateService;

    @GetMapping
    @Operation(
            summary = "Listar plantillas públicas activas",
            description = "Devuelve todas las plantillas públicas activas."
    )
    public List<TaskTemplateResponse> getPublicTemplates() {
        return taskTemplateService.getPublicActiveTemplates();
    }

    @GetMapping("/{templateId}")
    @Operation(
            summary = "Obtener una plantilla pública por id",
            description = "Devuelve una plantilla pública activa por id."
    )
    public TaskTemplateResponse getPublicTemplateById(@PathVariable Long templateId) {
        return taskTemplateService.getPublicTemplateById(templateId);
    }
}