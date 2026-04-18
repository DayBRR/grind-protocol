package com.davidrr.grindprotocol.task.service;

import com.davidrr.grindprotocol.task.dto.CreateTaskCompletionRequest;
import com.davidrr.grindprotocol.task.dto.TaskCompletionResponse;

import java.util.List;

public interface TaskCompletionService {
    TaskCompletionResponse completeTask(Long userId, CreateTaskCompletionRequest request);
    List<TaskCompletionResponse> getTodayCompletions(Long userId);
}
