package com.davidrr.grindprotocol.reward.dto;

import com.davidrr.grindprotocol.reward.enums.RewardCategory;
import com.davidrr.grindprotocol.reward.enums.RewardRedemptionStatus;
import com.davidrr.grindprotocol.reward.enums.RewardType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardRedemptionResponse {

    private Long id;

    private Long rewardId;
    private String rewardName;
    private String rewardDescription;

    private RewardType rewardType;
    private RewardCategory rewardCategory;

    private RewardRedemptionStatus status;

    private Long costPaid;

    private LocalDateTime redeemedAt;
    private LocalDateTime usedAt;
    private LocalDateTime cancelledAt;

    private String notes;
}