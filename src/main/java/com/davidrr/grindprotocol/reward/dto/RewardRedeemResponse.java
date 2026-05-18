package com.davidrr.grindprotocol.reward.dto;

import com.davidrr.grindprotocol.reward.enums.RewardRedemptionStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardRedeemResponse {

    private Long redemptionId;

    private Long rewardId;
    private String rewardName;

    private Long costPaid;

    private Long remainingCorePoints;

    private RewardRedemptionStatus status;

    private LocalDateTime redeemedAt;
}