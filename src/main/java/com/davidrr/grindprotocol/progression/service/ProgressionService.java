package com.davidrr.grindprotocol.progression.service;

import com.davidrr.grindprotocol.task.model.TaskCompletion;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;

public interface ProgressionService {

    void applyTaskCompletionProgress(Long userId, TaskCompletion completion);

    void addProgressionRewards(UserProfile userProfile, Long xp, Long corePoints);
}