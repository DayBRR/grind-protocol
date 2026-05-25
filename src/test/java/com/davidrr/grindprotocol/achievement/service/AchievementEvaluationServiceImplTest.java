package com.davidrr.grindprotocol.achievement.service;

import com.davidrr.grindprotocol.achievement.enums.AchievementType;
import com.davidrr.grindprotocol.achievement.model.Achievement;
import com.davidrr.grindprotocol.achievement.model.UserAchievement;
import com.davidrr.grindprotocol.achievement.repository.AchievementRepository;
import com.davidrr.grindprotocol.achievement.repository.UserAchievementRepository;
import com.davidrr.grindprotocol.achievement.service.impl.AchievementEvaluationServiceImpl;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementEvaluationServiceImplTest {

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private AchievementEvaluationServiceImpl achievementEvaluationService;

    @Test
    @DisplayName("evaluateAchievements debe crear y desbloquear achievement si se alcanza el objetivo")
    void evaluateAchievements_shouldCreateAndUnlockAchievementWhenTargetReached() {
        UserProfile userProfile = userProfile(1L);
        userProfile.setTotalXp(150L);

        Achievement achievement = achievement(
                4L,
                "TOTAL_XP_100",
                AchievementType.TOTAL_XP,
                100L
        );

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(userProfile));

        when(achievementRepository.findByEnabledTrueOrderByIdAsc())
                .thenReturn(List.of(achievement));

        when(userAchievementRepository.findByUserProfileUserIdAndAchievementId(1L, 4L))
                .thenReturn(Optional.empty());

        ArgumentCaptor<UserAchievement> captor =
                ArgumentCaptor.forClass(UserAchievement.class);

        achievementEvaluationService.evaluateAchievements(1L);

        verify(userAchievementRepository).save(captor.capture());

        UserAchievement saved = captor.getValue();

        assertThat(saved.getUserProfile()).isEqualTo(userProfile);
        assertThat(saved.getAchievement()).isEqualTo(achievement);
        assertThat(saved.getProgressValue()).isEqualTo(150L);
        assertThat(saved.getUnlocked()).isTrue();
        assertThat(saved.getUnlockedAt()).isNotNull();
        assertThat(saved.getClaimed()).isFalse();
    }

    @Test
    @DisplayName("evaluateAchievements debe crear achievement sin desbloquear si no se alcanza el objetivo")
    void evaluateAchievements_shouldCreateAchievementWithoutUnlockingWhenTargetNotReached() {
        UserProfile userProfile = userProfile(1L);
        userProfile.setTotalXp(50L);

        Achievement achievement = achievement(
                4L,
                "TOTAL_XP_100",
                AchievementType.TOTAL_XP,
                100L
        );

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(userProfile));

        when(achievementRepository.findByEnabledTrueOrderByIdAsc())
                .thenReturn(List.of(achievement));

        when(userAchievementRepository.findByUserProfileUserIdAndAchievementId(1L, 4L))
                .thenReturn(Optional.empty());

        ArgumentCaptor<UserAchievement> captor =
                ArgumentCaptor.forClass(UserAchievement.class);

        achievementEvaluationService.evaluateAchievements(1L);

        verify(userAchievementRepository).save(captor.capture());

        UserAchievement saved = captor.getValue();

        assertThat(saved.getProgressValue()).isEqualTo(50L);
        assertThat(saved.getUnlocked()).isFalse();
        assertThat(saved.getUnlockedAt()).isNull();
        assertThat(saved.getClaimed()).isFalse();
    }

    @Test
    @DisplayName("evaluateAchievements debe actualizar progreso de achievement existente")
    void evaluateAchievements_shouldUpdateExistingUserAchievementProgress() {
        UserProfile userProfile = userProfile(1L);
        userProfile.setTotalXp(75L);

        Achievement achievement = achievement(
                4L,
                "TOTAL_XP_100",
                AchievementType.TOTAL_XP,
                100L
        );

        UserAchievement existing = new UserAchievement();
        existing.setId(10L);
        existing.setUserProfile(userProfile);
        existing.setAchievement(achievement);
        existing.setProgressValue(20L);
        existing.setUnlocked(false);
        existing.setClaimed(false);

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(userProfile));

        when(achievementRepository.findByEnabledTrueOrderByIdAsc())
                .thenReturn(List.of(achievement));

        when(userAchievementRepository.findByUserProfileUserIdAndAchievementId(1L, 4L))
                .thenReturn(Optional.of(existing));

        achievementEvaluationService.evaluateAchievements(1L);

        assertThat(existing.getProgressValue()).isEqualTo(75L);
        assertThat(existing.getUnlocked()).isFalse();
        assertThat(existing.getUnlockedAt()).isNull();

        verify(userAchievementRepository).save(existing);
    }

    @Test
    @DisplayName("evaluateAchievements no debe sobrescribir unlockedAt si ya estaba desbloqueado")
    void evaluateAchievements_shouldNotOverrideUnlockedAtWhenAlreadyUnlocked() {
        UserProfile userProfile = userProfile(1L);
        userProfile.setTotalXp(200L);

        Achievement achievement = achievement(
                4L,
                "TOTAL_XP_100",
                AchievementType.TOTAL_XP,
                100L
        );

        LocalDateTime originalUnlockedAt = LocalDateTime.now().minusDays(3);

        UserAchievement existing = new UserAchievement();
        existing.setId(10L);
        existing.setUserProfile(userProfile);
        existing.setAchievement(achievement);
        existing.setProgressValue(150L);
        existing.setUnlocked(true);
        existing.setUnlockedAt(originalUnlockedAt);
        existing.setClaimed(false);

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(userProfile));

        when(achievementRepository.findByEnabledTrueOrderByIdAsc())
                .thenReturn(List.of(achievement));

        when(userAchievementRepository.findByUserProfileUserIdAndAchievementId(1L, 4L))
                .thenReturn(Optional.of(existing));

        achievementEvaluationService.evaluateAchievements(1L);

        assertThat(existing.getProgressValue()).isEqualTo(200L);
        assertThat(existing.getUnlocked()).isTrue();
        assertThat(existing.getUnlockedAt()).isEqualTo(originalUnlockedAt);

        verify(userAchievementRepository).save(existing);
    }

    @Test
    @DisplayName("evaluateAchievements debe calcular LEVEL_REACHED")
    void evaluateAchievements_shouldCalculateLevelReachedProgress() {
        UserProfile userProfile = userProfile(1L);
        userProfile.setLevel(5);

        Achievement achievement = achievement(
                5L,
                "LEVEL_5",
                AchievementType.LEVEL_REACHED,
                5L
        );

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(userProfile));

        when(achievementRepository.findByEnabledTrueOrderByIdAsc())
                .thenReturn(List.of(achievement));

        when(userAchievementRepository.findByUserProfileUserIdAndAchievementId(1L, 5L))
                .thenReturn(Optional.empty());

        ArgumentCaptor<UserAchievement> captor =
                ArgumentCaptor.forClass(UserAchievement.class);

        achievementEvaluationService.evaluateAchievements(1L);

        verify(userAchievementRepository).save(captor.capture());

        UserAchievement saved = captor.getValue();

        assertThat(saved.getProgressValue()).isEqualTo(5L);
        assertThat(saved.getUnlocked()).isTrue();
    }

    @Test
    @DisplayName("evaluateAchievements debe calcular CURRENT_STREAK")
    void evaluateAchievements_shouldCalculateCurrentStreakProgress() {
        UserProfile userProfile = userProfile(1L);
        userProfile.setCurrentStreak(7);

        Achievement achievement = achievement(
                6L,
                "STREAK_7",
                AchievementType.CURRENT_STREAK,
                7L
        );

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(userProfile));

        when(achievementRepository.findByEnabledTrueOrderByIdAsc())
                .thenReturn(List.of(achievement));

        when(userAchievementRepository.findByUserProfileUserIdAndAchievementId(1L, 6L))
                .thenReturn(Optional.empty());

        ArgumentCaptor<UserAchievement> captor =
                ArgumentCaptor.forClass(UserAchievement.class);

        achievementEvaluationService.evaluateAchievements(1L);

        verify(userAchievementRepository).save(captor.capture());

        UserAchievement saved = captor.getValue();

        assertThat(saved.getProgressValue()).isEqualTo(7L);
        assertThat(saved.getUnlocked()).isTrue();
    }

    @Test
    @DisplayName("evaluateAchievements debe calcular BEST_STREAK")
    void evaluateAchievements_shouldCalculateBestStreakProgress() {
        UserProfile userProfile = userProfile(1L);
        userProfile.setBestStreak(10);

        Achievement achievement = achievement(
                7L,
                "BEST_STREAK_10",
                AchievementType.BEST_STREAK,
                10L
        );

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(userProfile));

        when(achievementRepository.findByEnabledTrueOrderByIdAsc())
                .thenReturn(List.of(achievement));

        when(userAchievementRepository.findByUserProfileUserIdAndAchievementId(1L, 7L))
                .thenReturn(Optional.empty());

        ArgumentCaptor<UserAchievement> captor =
                ArgumentCaptor.forClass(UserAchievement.class);

        achievementEvaluationService.evaluateAchievements(1L);

        verify(userAchievementRepository).save(captor.capture());

        UserAchievement saved = captor.getValue();

        assertThat(saved.getProgressValue()).isEqualTo(10L);
        assertThat(saved.getUnlocked()).isTrue();
    }

    private Achievement achievement(
            Long id,
            String code,
            AchievementType type,
            Long targetValue
    ) {
        Achievement achievement = new Achievement();
        achievement.setId(id);
        achievement.setCode(code);
        achievement.setName(code);
        achievement.setDescription("Description");
        achievement.setType(type);
        achievement.setTargetValue(targetValue);
        achievement.setXpReward(0L);
        achievement.setCorePointsReward(0L);
        achievement.setEnabled(true);
        achievement.setHidden(false);
        return achievement;
    }

    private UserProfile userProfile(Long userId) {
        User user = User.builder()
                .id(userId)
                .username("david")
                .email("david@test.com")
                .password("encoded")
                .role("USER")
                .enabled(true)
                .build();

        return UserProfile.builder()
                .id(20L)
                .user(user)
                .displayName("David")
                .dailyTaskGoal(3)
                .totalXp(0L)
                .level(1)
                .corePoints(0L)
                .currentStreak(0)
                .bestStreak(0)
                .build();
    }
}