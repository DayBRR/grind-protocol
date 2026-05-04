package com.davidrr.grindprotocol.progression.service;

import com.davidrr.grindprotocol.task.model.TaskCompletion;

public interface ProgressionService {
    void applyTaskCompletionProgress(Long userId, TaskCompletion completion);
}