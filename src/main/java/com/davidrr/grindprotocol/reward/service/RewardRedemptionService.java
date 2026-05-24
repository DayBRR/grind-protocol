package com.davidrr.grindprotocol.reward.service;

import com.davidrr.grindprotocol.reward.dto.RewardRedeemResponse;
import com.davidrr.grindprotocol.reward.dto.RewardRedemptionResponse;

public interface RewardRedemptionService {

    RewardRedeemResponse redeemReward(Long rewardId, Long userId);

    RewardRedemptionResponse useRedemption(Long redemptionId, Long userId);
}