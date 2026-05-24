package com.davidrr.grindprotocol.reward.service.impl;

import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.common.exception.ErrorMessages;
import com.davidrr.grindprotocol.reward.dto.RewardRedeemResponse;
import com.davidrr.grindprotocol.reward.dto.RewardRedemptionResponse;
import com.davidrr.grindprotocol.reward.enums.RewardRedemptionStatus;
import com.davidrr.grindprotocol.reward.mapper.RewardMapper;
import com.davidrr.grindprotocol.reward.model.Reward;
import com.davidrr.grindprotocol.reward.model.RewardRedemption;
import com.davidrr.grindprotocol.reward.repository.RewardRedemptionRepository;
import com.davidrr.grindprotocol.reward.repository.RewardRepository;
import com.davidrr.grindprotocol.reward.service.RewardRedemptionService;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RewardRedemptionServiceImpl implements RewardRedemptionService {

    private final RewardRepository rewardRepository;
    private final RewardRedemptionRepository rewardRedemptionRepository;
    private final UserProfileRepository userProfileRepository;
    private final RewardMapper rewardMapper;

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
        validateRepeatability(reward, userId);
        validateCooldown(reward, userId);
        validateEnoughCorePoints(userProfile, reward);

        Long currentCorePoints = userProfile.getCorePoints();
        userProfile.setCorePoints(currentCorePoints - reward.getCostCorePoints());

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

    private void validateEnoughCorePoints(UserProfile userProfile, Reward reward) {
        Long currentCorePoints = userProfile.getCorePoints();
        if (currentCorePoints < reward.getCostCorePoints()) {
            throw new BusinessException(
                    ErrorCodes.Reward.NOT_ENOUGH_CORE_POINTS,
                    ErrorMessages.Reward.NOT_ENOUGH_CORE_POINTS
            );
        }
    }

    @Override
    @Transactional
    public RewardRedemptionResponse useRedemption(Long redemptionId, Long userId) {

        RewardRedemption redemption = rewardRedemptionRepository
                .findByIdAndUserProfileUserId(redemptionId, userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.Reward.REDEMPTION_NOT_FOUND,
                        ErrorMessages.Reward.REDEMPTION_NOT_FOUND
                ));

        if (redemption.getStatus() != RewardRedemptionStatus.REDEEMED) {
            throw new BusinessException(
                    ErrorCodes.Reward.REDEMPTION_NOT_USABLE,
                    ErrorMessages.Reward.REDEMPTION_NOT_USABLE
            );
        }

        redemption.setStatus(RewardRedemptionStatus.USED);
        redemption.setUsedAt(LocalDateTime.now());

        RewardRedemption savedRedemption = rewardRedemptionRepository.save(redemption);

        return rewardMapper.toRedemptionResponse(savedRedemption);
    }

    private void validateRepeatability(
            Reward reward,
            Long userId
    ) {

        if (Boolean.TRUE.equals(reward.getRepeatable())) {
            return;
        }

        boolean alreadyRedeemed = rewardRedemptionRepository
                .existsByRewardIdAndUserProfileUserIdAndStatusIn(
                        reward.getId(), userId,
                        List.of(RewardRedemptionStatus.REDEEMED, RewardRedemptionStatus.USED, RewardRedemptionStatus.EXPIRED)
                );

        if (alreadyRedeemed) {
            throw new BusinessException(
                    ErrorCodes.Reward.NOT_REPEATABLE,
                    ErrorMessages.Reward.NOT_REPEATABLE
            );
        }
    }

    private void validateCooldown(
            Reward reward,
            Long userId
    ) {
        if (reward.getCooldownDays() == null
                || reward.getCooldownDays() <= 0) {
            return;
        }

        Optional<RewardRedemption> latestRedemption =
                rewardRedemptionRepository
                        .findTopByRewardIdAndUserProfileUserIdAndStatusInOrderByRedeemedAtDesc(
                                reward.getId(),
                                userId,
                                List.of(
                                        RewardRedemptionStatus.REDEEMED,
                                        RewardRedemptionStatus.USED,
                                        RewardRedemptionStatus.EXPIRED
                                )
                        );

        if (latestRedemption.isEmpty()) {
            return;
        }

        LocalDateTime nextAvailableDate =
                latestRedemption.get()
                        .getRedeemedAt()
                        .plusDays(reward.getCooldownDays());

        if (LocalDateTime.now().isBefore(nextAvailableDate)) {
            throw new BusinessException(
                    ErrorCodes.Reward.COOLDOWN_ACTIVE,
                    ErrorMessages.Reward.COOLDOWN_ACTIVE
            );
        }
    }
}