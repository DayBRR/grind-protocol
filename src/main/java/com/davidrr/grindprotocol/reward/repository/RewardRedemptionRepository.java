package com.davidrr.grindprotocol.reward.repository;

import com.davidrr.grindprotocol.reward.enums.RewardRedemptionStatus;
import com.davidrr.grindprotocol.reward.model.RewardRedemption;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RewardRedemptionRepository extends JpaRepository<RewardRedemption, Long> {

    @EntityGraph(attributePaths = {"reward"})
    List<RewardRedemption> findByUserProfileUserIdOrderByRedeemedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"reward", "userProfile"})
    Optional<RewardRedemption> findByIdAndUserProfileUserId(Long id, Long userId);

    boolean existsByRewardIdAndUserProfileUserIdAndStatusIn(
            Long rewardId,
            Long userId,
            Collection<RewardRedemptionStatus> statuses
    );

    Optional<RewardRedemption> findTopByRewardIdAndUserProfileUserIdAndStatusInOrderByRedeemedAtDesc(
            Long rewardId,
            Long userId,
            Collection<RewardRedemptionStatus> statuses
    );
}