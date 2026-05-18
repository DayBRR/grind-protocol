package com.davidrr.grindprotocol.reward.controller;

import com.davidrr.grindprotocol.reward.dto.RewardRedeemResponse;
import com.davidrr.grindprotocol.reward.dto.RewardRedemptionResponse;
import com.davidrr.grindprotocol.reward.dto.RewardResponse;
import com.davidrr.grindprotocol.reward.service.RewardRedemptionService;
import com.davidrr.grindprotocol.reward.service.RewardService;
import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.davidrr.grindprotocol.utils.TestAuthenticatedUserFactory.defaultUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RewardControllerTest {

    @Test
    @DisplayName("getAvailableRewards debe delegar en el service")
    void getAvailableRewards_shouldDelegateToService() {
        RewardService rewardService = mock(RewardService.class);
        RewardRedemptionService rewardRedemptionService = mock(RewardRedemptionService.class);
        RewardController controller = new RewardController(rewardService, rewardRedemptionService);

        RewardResponse response = mock(RewardResponse.class);

        when(rewardService.getAvailableRewards()).thenReturn(List.of(response));

        List<RewardResponse> result = controller.getAvailableRewards();

        assertThat(result).containsExactly(response);
        verify(rewardService).getAvailableRewards();
        verifyNoInteractions(rewardRedemptionService);
    }

    @Test
    @DisplayName("getMyRedemptions debe delegar en el service usando el usuario autenticado")
    void getMyRedemptions_shouldDelegateToService() {
        RewardService rewardService = mock(RewardService.class);
        RewardRedemptionService rewardRedemptionService = mock(RewardRedemptionService.class);
        RewardController controller = new RewardController(rewardService, rewardRedemptionService);

        AuthenticatedUser currentUser = defaultUser();
        RewardRedemptionResponse response = mock(RewardRedemptionResponse.class);

        when(rewardService.getMyRedemptions(1L)).thenReturn(List.of(response));

        List<RewardRedemptionResponse> result = controller.getMyRedemptions(currentUser);

        assertThat(result).containsExactly(response);
        verify(rewardService).getMyRedemptions(1L);
        verifyNoInteractions(rewardRedemptionService);
    }

    @Test
    @DisplayName("redeemReward debe delegar en el service usando rewardId y usuario autenticado")
    void redeemReward_shouldDelegateToService() {
        RewardService rewardService = mock(RewardService.class);
        RewardRedemptionService rewardRedemptionService = mock(RewardRedemptionService.class);
        RewardController controller = new RewardController(rewardService, rewardRedemptionService);

        AuthenticatedUser currentUser = defaultUser();
        RewardRedeemResponse response = mock(RewardRedeemResponse.class);

        when(rewardRedemptionService.redeemReward(10L, 1L)).thenReturn(response);

        RewardRedeemResponse result = controller.redeemReward(10L, currentUser);

        assertThat(result).isSameAs(response);
        verify(rewardRedemptionService).redeemReward(10L, 1L);
        verifyNoInteractions(rewardService);
    }
}