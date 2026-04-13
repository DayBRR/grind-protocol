package com.davidrr.grindprotocol.task.service;

import com.davidrr.grindprotocol.task.dto.CreateTaskFromTemplateRequest;
import com.davidrr.grindprotocol.task.dto.CreateTaskRequest;
import com.davidrr.grindprotocol.task.dto.TaskResponse;

import java.util.List;

public interface TaskService {

    TaskResponse createTask(Long userId, CreateTaskRequest request);

    TaskResponse createTaskFromTemplate(Long userId, CreateTaskFromTemplateRequest request);

    List<TaskResponse> getActiveTasksByUser(Long userId);

    TaskResponse getTaskById(Long userId, Long taskId);
}