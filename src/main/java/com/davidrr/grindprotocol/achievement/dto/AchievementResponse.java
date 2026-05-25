package com.davidrr.grindprotocol.achievement.dto;

import com.davidrr.grindprotocol.achievement.enums.AchievementType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementResponse {

    private Long achievementId;

    private String code;

    private String name;

    private String description;

    private AchievementType type;

    private Long targetValue;

    private Long progressValue;

    private Long xpReward;

    private Long corePointsReward;

    private Boolean hidden;

    private Boolean unlocked;

    private LocalDateTime unlockedAt;

    private Boolean claimed;

    private LocalDateTime claimedAt;
}