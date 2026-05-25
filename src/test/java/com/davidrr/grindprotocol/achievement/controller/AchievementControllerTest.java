package com.davidrr.grindprotocol.achievement.controller;

import com.davidrr.grindprotocol.achievement.dto.AchievementClaimResponse;
import com.davidrr.grindprotocol.achievement.dto.AchievementResponse;
import com.davidrr.grindprotocol.achievement.service.AchievementService;
import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.davidrr.grindprotocol.utils.TestAuthenticatedUserFactory.defaultUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AchievementControllerTest {

    @Test
    @DisplayName("getAchievements debe delegar en el service usando el usuario autenticado")
    void getAchievements_shouldDelegateToService() {
        AchievementService achievementService = mock(AchievementService.class);
        AchievementController controller = new AchievementController(achievementService);

        AuthenticatedUser currentUser = defaultUser();
        AchievementResponse response = mock(AchievementResponse.class);

        when(achievementService.getAchievements(1L))
                .thenReturn(List.of(response));

        List<AchievementResponse> result =
                controller.getAchievements(currentUser);

        assertThat(result).containsExactly(response);

        verify(achievementService).getAchievements(1L);
    }

    @Test
    @DisplayName("evaluateAchievements debe delegar en el service usando el usuario autenticado")
    void evaluateAchievements_shouldDelegateToService() {
        AchievementService achievementService = mock(AchievementService.class);
        AchievementController controller = new AchievementController(achievementService);

        AuthenticatedUser currentUser = defaultUser();

        controller.evaluateAchievements(currentUser);

        verify(achievementService).evaluateAchievements(1L);
    }

    @Test
    @DisplayName("claimAchievement debe delegar en el service usando achievementId y usuario autenticado")
    void claimAchievement_shouldDelegateToService() {
        AchievementService achievementService = mock(AchievementService.class);
        AchievementController controller = new AchievementController(achievementService);

        AuthenticatedUser currentUser = defaultUser();
        AchievementClaimResponse response = mock(AchievementClaimResponse.class);

        when(achievementService.claimAchievement(4L, 1L))
                .thenReturn(response);

        AchievementClaimResponse result =
                controller.claimAchievement(currentUser, 4L);

        assertThat(result).isSameAs(response);

        verify(achievementService).claimAchievement(4L, 1L);
    }
}