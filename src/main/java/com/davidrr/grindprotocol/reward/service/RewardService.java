package com.davidrr.grindprotocol.reward.service;

import com.davidrr.grindprotocol.reward.dto.RewardRedemptionResponse;
import com.davidrr.grindprotocol.reward.dto.RewardResponse;

import java.util.List;

public interface RewardService {

    List<RewardResponse> getAvailableRewards();

    List<RewardRedemptionResponse> getMyRedemptions(Long userId);
}