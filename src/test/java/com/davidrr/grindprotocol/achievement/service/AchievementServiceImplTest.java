package com.davidrr.grindprotocol.achievement.service;

import com.davidrr.grindprotocol.achievement.dto.AchievementClaimResponse;
import com.davidrr.grindprotocol.achievement.dto.AchievementResponse;
import com.davidrr.grindprotocol.achievement.enums.AchievementType;
import com.davidrr.grindprotocol.achievement.mapper.AchievementMapper;
import com.davidrr.grindprotocol.achievement.model.Achievement;
import com.davidrr.grindprotocol.achievement.model.UserAchievement;
import com.davidrr.grindprotocol.achievement.repository.AchievementRepository;
import com.davidrr.grindprotocol.achievement.repository.UserAchievementRepository;
import com.davidrr.grindprotocol.achievement.service.impl.AchievementServiceImpl;
import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.progression.service.ProgressionService;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceImplTest {

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private AchievementMapper achievementMapper;

    @Mock
    private AchievementEvaluationService achievementEvaluationService;

    @Mock
    private ProgressionService progressionService;

    @InjectMocks
    private AchievementServiceImpl achievementService;

    @Test
    @DisplayName("getAchievements debe devolver achievements mapeados con estado de usuario")
    void getAchievements_shouldReturnMappedAchievementsWithUserState() {
        Achievement achievement1 = achievement(1L, "TOTAL_XP_100", AchievementType.TOTAL_XP, 100L);
        Achievement achievement2 = achievement(2L, "LEVEL_5", AchievementType.LEVEL_REACHED, 5L);

        UserAchievement userAchievement = userAchievement(achievement1, userProfile(1L));
        userAchievement.setProgressValue(150L);
        userAchievement.setUnlocked(true);

        AchievementResponse response1 = mock(AchievementResponse.class);
        AchievementResponse response2 = mock(AchievementResponse.class);

        when(achievementRepository.findByEnabledTrueOrderByIdAsc())
                .thenReturn(List.of(achievement1, achievement2));

        when(userAchievementRepository.findByUserProfileUserIdOrderByUnlockedAtDesc(1L))
                .thenReturn(List.of(userAchievement));

        when(achievementMapper.toResponse(achievement1, userAchievement))
                .thenReturn(response1);

        when(achievementMapper.toResponse(achievement2, null))
                .thenReturn(response2);

        List<AchievementResponse> result = achievementService.getAchievements(1L);

        assertThat(result).containsExactly(response1, response2);

        verify(achievementRepository).findByEnabledTrueOrderByIdAsc();
        verify(userAchievementRepository).findByUserProfileUserIdOrderByUnlockedAtDesc(1L);
        verify(achievementMapper).toResponse(achievement1, userAchievement);
        verify(achievementMapper).toResponse(achievement2, null);
    }

    @Test
    @DisplayName("evaluateAchievements debe delegar en AchievementEvaluationService")
    void evaluateAchievements_shouldDelegateToEvaluationService() {
        achievementService.evaluateAchievements(1L);

        verify(achievementEvaluationService).evaluateAchievements(1L);
    }

    @Test
    @DisplayName("claimAchievement debe reclamar un achievement desbloqueado")
    void claimAchievement_shouldClaimUnlockedAchievement() {
        Achievement achievement = achievement(4L, "TOTAL_XP_100", AchievementType.TOTAL_XP, 100L);
        achievement.setName("Getting Started");
        achievement.setXpReward(25L);
        achievement.setCorePointsReward(5L);

        UserProfile userProfile = userProfile(1L);

        UserAchievement userAchievement = userAchievement(achievement, userProfile);
        userAchievement.setUnlocked(true);
        userAchievement.setClaimed(false);

        when(userAchievementRepository.findByUserProfileUserIdAndAchievementId(1L, 4L))
                .thenReturn(Optional.of(userAchievement));

        AchievementClaimResponse result =
                achievementService.claimAchievement(4L, 1L);

        assertThat(result.getAchievementId()).isEqualTo(4L);
        assertThat(result.getCode()).isEqualTo("TOTAL_XP_100");
        assertThat(result.getName()).isEqualTo("Getting Started");
        assertThat(result.getXpReward()).isEqualTo(25L);
        assertThat(result.getCorePointsReward()).isEqualTo(5L);
        assertThat(result.getClaimed()).isTrue();

        assertThat(userAchievement.getClaimed()).isTrue();
        assertThat(userAchievement.getClaimedAt()).isNotNull();

        verify(progressionService).addProgressionRewards(
                userProfile,
                25L,
                5L
        );

        verify(userAchievementRepository).save(userAchievement);
    }

    @Test
    @DisplayName("claimAchievement debe fallar si el achievement no existe para el usuario")
    void claimAchievement_shouldThrowWhenAchievementNotFoundForUser() {
        when(userAchievementRepository.findByUserProfileUserIdAndAchievementId(1L, 4L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> achievementService.claimAchievement(4L, 1L))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(progressionService);
        verify(userAchievementRepository, never()).save(any());
    }

    @Test
    @DisplayName("claimAchievement debe fallar si el achievement no está desbloqueado")
    void claimAchievement_shouldThrowWhenAchievementIsNotUnlocked() {
        Achievement achievement = achievement(4L, "TOTAL_XP_100", AchievementType.TOTAL_XP, 100L);
        UserProfile userProfile = userProfile(1L);

        UserAchievement userAchievement = userAchievement(achievement, userProfile);
        userAchievement.setUnlocked(false);
        userAchievement.setClaimed(false);

        when(userAchievementRepository.findByUserProfileUserIdAndAchievementId(1L, 4L))
                .thenReturn(Optional.of(userAchievement));

        assertThatThrownBy(() -> achievementService.claimAchievement(4L, 1L))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(progressionService);
        verify(userAchievementRepository, never()).save(any());
    }

    @Test
    @DisplayName("claimAchievement debe fallar si el achievement ya fue reclamado")
    void claimAchievement_shouldThrowWhenAchievementIsAlreadyClaimed() {
        Achievement achievement = achievement(4L, "TOTAL_XP_100", AchievementType.TOTAL_XP, 100L);
        UserProfile userProfile = userProfile(1L);

        UserAchievement userAchievement = userAchievement(achievement, userProfile);
        userAchievement.setUnlocked(true);
        userAchievement.setClaimed(true);

        when(userAchievementRepository.findByUserProfileUserIdAndAchievementId(1L, 4L))
                .thenReturn(Optional.of(userAchievement));

        assertThatThrownBy(() -> achievementService.claimAchievement(4L, 1L))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(progressionService);
        verify(userAchievementRepository, never()).save(any());
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

    private UserAchievement userAchievement(
            Achievement achievement,
            UserProfile userProfile
    ) {
        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setId(10L);
        userAchievement.setAchievement(achievement);
        userAchievement.setUserProfile(userProfile);
        userAchievement.setProgressValue(0L);
        userAchievement.setUnlocked(false);
        userAchievement.setClaimed(false);
        return userAchievement;
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
                .totalXp(150L)
                .level(2)
                .corePoints(10L)
                .currentStreak(3)
                .bestStreak(5)
                .build();
    }
}