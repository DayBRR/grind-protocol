package com.davidrr.grindprotocol.reward.service;

import com.davidrr.grindprotocol.reward.dto.RewardRedeemResponse;

public interface RewardRedemptionService {

    RewardRedeemResponse redeemReward(Long rewardId, Long userId);
}