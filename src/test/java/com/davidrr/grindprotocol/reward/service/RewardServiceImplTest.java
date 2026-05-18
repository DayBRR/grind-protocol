package com.davidrr.grindprotocol.reward.service;

import com.davidrr.grindprotocol.reward.dto.RewardRedemptionResponse;
import com.davidrr.grindprotocol.reward.dto.RewardResponse;
import com.davidrr.grindprotocol.reward.mapper.RewardMapper;
import com.davidrr.grindprotocol.reward.model.Reward;
import com.davidrr.grindprotocol.reward.model.RewardRedemption;
import com.davidrr.grindprotocol.reward.repository.RewardRedemptionRepository;
import com.davidrr.grindprotocol.reward.repository.RewardRepository;
import com.davidrr.grindprotocol.reward.service.impl.RewardServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardServiceImplTest {

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private RewardRedemptionRepository rewardRedemptionRepository;

    @Mock
    private RewardMapper rewardMapper;

    @InjectMocks
    private RewardServiceImpl rewardService;

    @Test
    @DisplayName("getAvailableRewards debe devolver recompensas activas mapeadas")
    void getAvailableRewards_shouldReturnMappedEnabledRewards() {
        Reward reward1 = mock(Reward.class);
        Reward reward2 = mock(Reward.class);

        RewardResponse response1 = mock(RewardResponse.class);
        RewardResponse response2 = mock(RewardResponse.class);

        when(rewardRepository.findByEnabledTrueOrderByCostCorePointsAsc())
                .thenReturn(List.of(reward1, reward2));

        when(rewardMapper.toResponse(reward1)).thenReturn(response1);
        when(rewardMapper.toResponse(reward2)).thenReturn(response2);

        List<RewardResponse> result = rewardService.getAvailableRewards();

        assertThat(result).containsExactly(response1, response2);

        verify(rewardRepository).findByEnabledTrueOrderByCostCorePointsAsc();
        verify(rewardMapper).toResponse(reward1);
        verify(rewardMapper).toResponse(reward2);
        verifyNoInteractions(rewardRedemptionRepository);
    }

    @Test
    @DisplayName("getMyRedemptions debe devolver el historial del usuario mapeado")
    void getMyRedemptions_shouldReturnMappedUserRedemptions() {
        RewardRedemption redemption1 = mock(RewardRedemption.class);
        RewardRedemption redemption2 = mock(RewardRedemption.class);

        RewardRedemptionResponse response1 = mock(RewardRedemptionResponse.class);
        RewardRedemptionResponse response2 = mock(RewardRedemptionResponse.class);

        when(rewardRedemptionRepository.findByUserProfileUserIdOrderByRedeemedAtDesc(1L))
                .thenReturn(List.of(redemption1, redemption2));

        when(rewardMapper.toRedemptionResponse(redemption1)).thenReturn(response1);
        when(rewardMapper.toRedemptionResponse(redemption2)).thenReturn(response2);

        List<RewardRedemptionResponse> result = rewardService.getMyRedemptions(1L);

        assertThat(result).containsExactly(response1, response2);

        verify(rewardRedemptionRepository).findByUserProfileUserIdOrderByRedeemedAtDesc(1L);
        verify(rewardMapper).toRedemptionResponse(redemption1);
        verify(rewardMapper).toRedemptionResponse(redemption2);
        verifyNoInteractions(rewardRepository);
    }
}