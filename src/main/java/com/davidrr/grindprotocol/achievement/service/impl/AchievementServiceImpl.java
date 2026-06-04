package com.davidrr.grindprotocol.achievement.service.impl;

import com.davidrr.grindprotocol.achievement.dto.AchievementClaimResponse;
import com.davidrr.grindprotocol.achievement.dto.AchievementResponse;
import com.davidrr.grindprotocol.achievement.mapper.AchievementMapper;
import com.davidrr.grindprotocol.achievement.model.Achievement;
import com.davidrr.grindprotocol.achievement.model.UserAchievement;
import com.davidrr.grindprotocol.achievement.repository.AchievementRepository;
import com.davidrr.grindprotocol.achievement.repository.UserAchievementRepository;
import com.davidrr.grindprotocol.achievement.service.AchievementService;
import com.davidrr.grindprotocol.activity.service.UserActivityEventService;
import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.common.exception.ErrorMessages;
import com.davidrr.grindprotocol.progression.service.ProgressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final AchievementMapper achievementMapper;
    private final ProgressionService progressionService;
    private final UserActivityEventService userActivityEventService;

    @Override
    public List<AchievementResponse> getAchievements(Long userId) {

        List<Achievement> achievements =
                achievementRepository.findByEnabledTrueOrderByIdAsc();

        Map<Long, UserAchievement> userAchievements =
                userAchievementRepository
                        .findByUserProfileUserIdOrderByUnlockedAtDesc(userId)
                        .stream()
                        .collect(Collectors.toMap(
                                ua -> ua.getAchievement().getId(),
                                Function.identity()
                        ));

        return achievements.stream()
                .map(achievement ->
                        achievementMapper.toResponse(
                                achievement,
                                userAchievements.get(achievement.getId())
                        )
                )
                .toList();
    }

    @Override
    @Transactional
    public AchievementClaimResponse claimAchievement(
            Long achievementId,
            Long userId
    ) {

        UserAchievement userAchievement =
                userAchievementRepository
                        .findByUserProfileUserIdAndAchievementId(
                                userId,
                                achievementId
                        )
                        .orElseThrow(() -> new BusinessException(
                                ErrorCodes.Achievement.NOT_FOUND,
                                ErrorMessages.Achievement.NOT_FOUND
                        ));

        if (!Boolean.TRUE.equals(userAchievement.getUnlocked())) {

            throw new BusinessException(
                    ErrorCodes.Achievement.NOT_UNLOCKED,
                    ErrorMessages.Achievement.NOT_UNLOCKED
            );
        }

        if (Boolean.TRUE.equals(userAchievement.getClaimed())) {

            throw new BusinessException(
                    ErrorCodes.Achievement.ALREADY_CLAIMED,
                    ErrorMessages.Achievement.ALREADY_CLAIMED
            );
        }

        Achievement achievement =
                userAchievement.getAchievement();

        progressionService.addProgressionRewards(
                userAchievement.getUserProfile(),
                achievement.getXpReward(),
                achievement.getCorePointsReward()
        );

        userAchievement.setClaimed(true);
        userAchievement.setClaimedAt(LocalDateTime.now());

        UserAchievement savedUserAchievement = userAchievementRepository.save(userAchievement);
        userActivityEventService.recordAchievementClaimed(savedUserAchievement);

        return AchievementClaimResponse.builder()
                .achievementId(achievement.getId())
                .code(achievement.getCode())
                .name(achievement.getName())
                .xpReward(achievement.getXpReward())
                .corePointsReward(achievement.getCorePointsReward())
                .claimed(true)
                .build();
    }
}