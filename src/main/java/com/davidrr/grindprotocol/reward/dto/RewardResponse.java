package com.davidrr.grindprotocol.reward.dto;

import com.davidrr.grindprotocol.reward.enums.RewardCategory;
import com.davidrr.grindprotocol.reward.enums.RewardType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardResponse {

    private Long id;

    private String name;
    private String description;

    private RewardType type;
    private RewardCategory category;

    private Long costCorePoints;

    private Boolean enabled;
    private Boolean repeatable;

    private Integer cooldownDays;
    private Long requiredLevel;
    private Long requiredCurrentStreak;
}