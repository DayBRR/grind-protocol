package com.davidrr.grindprotocol.achievement.mapper;

import com.davidrr.grindprotocol.achievement.dto.AchievementResponse;
import com.davidrr.grindprotocol.achievement.model.Achievement;
import com.davidrr.grindprotocol.achievement.model.UserAchievement;
import org.springframework.stereotype.Component;

@Component
public class AchievementMapper {

    public AchievementResponse toResponse(
            Achievement achievement,
            UserAchievement userAchievement
    ) {

        return AchievementResponse.builder()
                .achievementId(achievement.getId())
                .code(achievement.getCode())
                .name(achievement.getName())
                .description(achievement.getDescription())
                .type(achievement.getType())
                .targetValue(achievement.getTargetValue())
                .progressValue(
                        userAchievement != null
                                ? userAchievement.getProgressValue()
                                : 0L
                )
                .xpReward(achievement.getXpReward())
                .corePointsReward(achievement.getCorePointsReward())
                .hidden(achievement.getHidden())
                .unlocked(
                        userAchievement != null
                                && Boolean.TRUE.equals(userAchievement.getUnlocked())
                )
                .unlockedAt(
                        userAchievement != null
                                ? userAchievement.getUnlockedAt()
                                : null
                )
                .claimed(
                        userAchievement != null
                                && Boolean.TRUE.equals(userAchievement.getClaimed())
                )
                .claimedAt(
                        userAchievement != null
                                ? userAchievement.getClaimedAt()
                                : null
                )
                .build();
    }
}