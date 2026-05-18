package com.davidrr.grindprotocol.reward.service.impl;

import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.common.exception.ErrorMessages;
import com.davidrr.grindprotocol.reward.dto.RewardRedeemResponse;
import com.davidrr.grindprotocol.reward.enums.RewardRedemptionStatus;
import com.davidrr.grindprotocol.reward.model.Reward;
import com.davidrr.grindprotocol.reward.model.RewardRedemption;
import com.davidrr.grindprotocol.reward.repository.RewardRedemptionRepository;
import com.davidrr.grindprotocol.reward.repository.RewardRepository;
import com.davidrr.grindprotocol.reward.service.RewardRedemptionService;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RewardRedemptionServiceImpl implements RewardRedemptionService {

    private final RewardRepository rewardRepository;
    private final RewardRedemptionRepository rewardRedemptionRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public RewardRedeemResponse redeemReward(Long rewardId, Long userId) {

        Reward reward = rewardRepository.findByIdAndEnabledTrue(rewardId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.Reward.NOT_FOUND,
                        ErrorMessages.Reward.NOT_FOUND
                ));

        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.UserProfile.NOT_FOUND,
                        ErrorMessages.UserProfile.NOT_FOUND
                ));

        validateRequirements(reward, userProfile);

        Long currentCorePoints = userProfile.getCorePoints();

        if (currentCorePoints < reward.getCostCorePoints()) {
            throw new BusinessException(
                    ErrorCodes.Reward.NOT_ENOUGH_CORE_POINTS,
                    ErrorMessages.Reward.NOT_ENOUGH_CORE_POINTS
            );
        }

        userProfile.setCorePoints(
                currentCorePoints - reward.getCostCorePoints()
        );

        RewardRedemption redemption = new RewardRedemption();

        redemption.setReward(reward);
        redemption.setUserProfile(userProfile);

        redemption.setStatus(RewardRedemptionStatus.REDEEMED);

        redemption.setCostPaid(reward.getCostCorePoints());

        redemption.setRedeemedAt(LocalDateTime.now());

        RewardRedemption savedRedemption =
                rewardRedemptionRepository.save(redemption);

        return RewardRedeemResponse.builder()
                .redemptionId(savedRedemption.getId())
                .rewardId(reward.getId())
                .rewardName(reward.getName())
                .costPaid(savedRedemption.getCostPaid())
                .remainingCorePoints(userProfile.getCorePoints())
                .status(savedRedemption.getStatus())
                .redeemedAt(savedRedemption.getRedeemedAt())
                .build();
    }

    private void validateRequirements(
            Reward reward,
            UserProfile userProfile
    ) {

        if (reward.getRequiredLevel() != null
                && userProfile.getLevel() < reward.getRequiredLevel()) {
            throw new BusinessException(
                    ErrorCodes.Reward.REQUIRED_LEVEL_NOT_REACHED,
                    ErrorMessages.Reward.REQUIRED_LEVEL_NOT_REACHED
            );
        }

        if (reward.getRequiredCurrentStreak() != null
                && userProfile.getCurrentStreak() < reward.getRequiredCurrentStreak()) {
            throw new BusinessException(
                    ErrorCodes.Reward.REQUIRED_STREAK_NOT_REACHED,
                    ErrorMessages.Reward.REQUIRED_STREAK_NOT_REACHED
            );
        }

    }
}