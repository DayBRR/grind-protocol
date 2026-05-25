package com.davidrr.grindprotocol.achievement.service.impl;

import com.davidrr.grindprotocol.achievement.enums.AchievementType;
import com.davidrr.grindprotocol.achievement.model.Achievement;
import com.davidrr.grindprotocol.achievement.model.UserAchievement;
import com.davidrr.grindprotocol.achievement.repository.AchievementRepository;
import com.davidrr.grindprotocol.achievement.repository.UserAchievementRepository;
import com.davidrr.grindprotocol.achievement.service.AchievementEvaluationService;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementEvaluationServiceImpl implements AchievementEvaluationService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public void evaluateAchievements(Long userId) {

        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow();

        List<Achievement> achievements =
                achievementRepository.findByEnabledTrueOrderByIdAsc();

        for (Achievement achievement : achievements) {
            evaluateAchievement(userProfile, achievement);
        }
    }

    private void evaluateAchievement(
            UserProfile userProfile,
            Achievement achievement
    ) {

        Long progressValue = calculateProgressValue(
                userProfile,
                achievement.getType()
        );

        UserAchievement userAchievement = userAchievementRepository
                .findByUserProfileUserIdAndAchievementId(
                        userProfile.getUser().getId(),
                        achievement.getId()
                )
                .orElseGet(() -> createUserAchievement(
                        userProfile,
                        achievement
                ));

        userAchievement.setProgressValue(progressValue);

        if (!Boolean.TRUE.equals(userAchievement.getUnlocked())
                && progressValue >= achievement.getTargetValue()) {

            userAchievement.setUnlocked(true);
            userAchievement.setUnlockedAt(LocalDateTime.now());
        }

        userAchievementRepository.save(userAchievement);
    }

    private UserAchievement createUserAchievement(
            UserProfile userProfile,
            Achievement achievement
    ) {

        UserAchievement userAchievement = new UserAchievement();

        userAchievement.setUserProfile(userProfile);
        userAchievement.setAchievement(achievement);
        userAchievement.setProgressValue(0L);
        userAchievement.setUnlocked(false);
        userAchievement.setClaimed(false);

        return userAchievement;
    }

    private Long calculateProgressValue(
            UserProfile userProfile,
            AchievementType type
    ) {

        return switch (type) {
            case TOTAL_XP -> userProfile.getTotalXp();
            case LEVEL_REACHED -> Long.valueOf(userProfile.getLevel());
            case CURRENT_STREAK -> Long.valueOf(userProfile.getCurrentStreak());
            case BEST_STREAK -> Long.valueOf(userProfile.getBestStreak());

            case TASK_COMPLETION_COUNT,
                 REWARD_REDEMPTION_COUNT,
                 CORE_POINTS_EARNED -> 0L;
        };
    }
}