package com.davidrr.grindprotocol.task.controller;

import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import com.davidrr.grindprotocol.task.dto.CreateTaskCompletionRequest;
import com.davidrr.grindprotocol.task.dto.TaskCompletionResponse;
import com.davidrr.grindprotocol.task.service.TaskCompletionService;
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
public class TaskCompletionController {

    private final TaskCompletionService taskCompletionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskCompletionResponse completeTask(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateTaskCompletionRequest request
    ) {
        return taskCompletionService.completeTask(
                currentUser.getId(),
                request
        );
    }

    @GetMapping("/today")
    public List<TaskCompletionResponse> getTodayCompletions(
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return taskCompletionService.getTodayCompletions(currentUser.getId());
    }
}