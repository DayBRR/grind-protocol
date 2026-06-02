package com.davidrr.grindprotocol.achievement.service;

import com.davidrr.grindprotocol.achievement.dto.AchievementClaimResponse;
import com.davidrr.grindprotocol.achievement.dto.AchievementResponse;

import java.util.List;

public interface AchievementService {

    List<AchievementResponse> getAchievements(Long userId);

    AchievementClaimResponse claimAchievement(
            Long achievementId,
            Long userId
    );
}