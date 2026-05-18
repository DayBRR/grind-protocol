package com.davidrr.grindprotocol.reward.service.impl;

import com.davidrr.grindprotocol.reward.dto.RewardRedemptionResponse;
import com.davidrr.grindprotocol.reward.dto.RewardResponse;
import com.davidrr.grindprotocol.reward.mapper.RewardMapper;
import com.davidrr.grindprotocol.reward.repository.RewardRedemptionRepository;
import com.davidrr.grindprotocol.reward.repository.RewardRepository;
import com.davidrr.grindprotocol.reward.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardServiceImpl implements RewardService {

    private final RewardRepository rewardRepository;
    private final RewardRedemptionRepository rewardRedemptionRepository;
    private final RewardMapper rewardMapper;

    @Override
    public List<RewardResponse> getAvailableRewards() {

        return rewardRepository.findByEnabledTrueOrderByCostCorePointsAsc()
                .stream()
                .map(rewardMapper::toResponse)
                .toList();
    }

    @Override
    public List<RewardRedemptionResponse> getMyRedemptions(Long userId) {

        return rewardRedemptionRepository
                .findByUserProfileUserIdOrderByRedeemedAtDesc(userId)
                .stream()
                .map(rewardMapper::toRedemptionResponse)
                .toList();
    }
}