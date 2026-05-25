package com.davidrr.grindprotocol.achievement.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementClaimResponse {

    private Long achievementId;
    private String code;
    private String name;
    private Long xpReward;
    private Long corePointsReward;
    private Boolean claimed;
}