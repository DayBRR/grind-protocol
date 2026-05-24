package com.davidrr.grindprotocol.reward.service;

import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.reward.dto.RewardRedeemResponse;
import com.davidrr.grindprotocol.reward.dto.RewardRedemptionResponse;
import com.davidrr.grindprotocol.reward.enums.RewardCategory;
import com.davidrr.grindprotocol.reward.enums.RewardRedemptionStatus;
import com.davidrr.grindprotocol.reward.enums.RewardType;
import com.davidrr.grindprotocol.reward.mapper.RewardMapper;
import com.davidrr.grindprotocol.reward.model.Reward;
import com.davidrr.grindprotocol.reward.model.RewardRedemption;
import com.davidrr.grindprotocol.reward.repository.RewardRedemptionRepository;
import com.davidrr.grindprotocol.reward.repository.RewardRepository;
import com.davidrr.grindprotocol.reward.service.impl.RewardRedemptionServiceImpl;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardRedemptionServiceImplTest {

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private RewardRedemptionRepository rewardRedemptionRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private RewardRedemptionServiceImpl rewardRedemptionService;

    @Mock
    private RewardMapper rewardMapper;


    @Test
    @DisplayName("redeemReward debe descontar Core Points y crear redemption")
    void redeemReward_shouldSpendCorePointsAndCreateRedemption() {
        Reward reward = reward(10L, 20L);
        UserProfile userProfile = userProfile(1L, 100L, 3, 5);

        when(rewardRepository.findByIdAndEnabledTrue(10L))
                .thenReturn(Optional.of(reward));

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(userProfile));

        ArgumentCaptor<RewardRedemption> redemptionCaptor =
                ArgumentCaptor.forClass(RewardRedemption.class);

        when(rewardRedemptionRepository.save(redemptionCaptor.capture()))
                .thenAnswer(invocation -> {
                    RewardRedemption redemption = invocation.getArgument(0);
                    redemption.setId(50L);
                    return redemption;
                });

        RewardRedeemResponse result =
                rewardRedemptionService.redeemReward(10L, 1L);

        assertThat(userProfile.getCorePoints()).isEqualTo(80L);

        RewardRedemption saved = redemptionCaptor.getValue();
        assertThat(saved.getReward()).isEqualTo(reward);
        assertThat(saved.getUserProfile()).isEqualTo(userProfile);
        assertThat(saved.getStatus()).isEqualTo(RewardRedemptionStatus.REDEEMED);
        assertThat(saved.getCostPaid()).isEqualTo(20L);
        assertThat(saved.getRedeemedAt()).isNotNull();
        assertThat(saved.getUsedAt()).isNull();
        assertThat(saved.getCancelledAt()).isNull();

        assertThat(result.getRedemptionId()).isEqualTo(50L);
        assertThat(result.getRewardId()).isEqualTo(10L);
        assertThat(result.getRewardName()).isEqualTo("2 horas de gaming");
        assertThat(result.getCostPaid()).isEqualTo(20L);
        assertThat(result.getRemainingCorePoints()).isEqualTo(80L);
        assertThat(result.getStatus()).isEqualTo(RewardRedemptionStatus.REDEEMED);
        assertThat(result.getRedeemedAt()).isNotNull();

        verify(rewardRepository).findByIdAndEnabledTrue(10L);
        verify(userProfileRepository).findByUserId(1L);
        verify(rewardRedemptionRepository).save(any(RewardRedemption.class));
    }

    @Test
    @DisplayName("redeemReward debe fallar si la recompensa no existe o no está activa")
    void redeemReward_shouldThrowWhenRewardNotFound() {
        when(rewardRepository.findByIdAndEnabledTrue(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> rewardRedemptionService.redeemReward(10L, 1L))
                .isInstanceOf(BusinessException.class);

        verify(rewardRepository).findByIdAndEnabledTrue(10L);
        verifyNoInteractions(userProfileRepository);
        verifyNoInteractions(rewardRedemptionRepository);
    }

    @Test
    @DisplayName("redeemReward debe fallar si el perfil de usuario no existe")
    void redeemReward_shouldThrowWhenUserProfileNotFound() {
        Reward reward = reward(10L, 20L);

        when(rewardRepository.findByIdAndEnabledTrue(10L))
                .thenReturn(Optional.of(reward));

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> rewardRedemptionService.redeemReward(10L, 1L))
                .isInstanceOf(BusinessException.class);

        verify(rewardRepository).findByIdAndEnabledTrue(10L);
        verify(userProfileRepository).findByUserId(1L);
        verifyNoInteractions(rewardRedemptionRepository);
    }

    @Test
    @DisplayName("redeemReward debe fallar si no hay Core Points suficientes")
    void redeemReward_shouldThrowWhenNotEnoughCorePoints() {
        Reward reward = reward(10L, 20L);
        UserProfile userProfile = userProfile(1L, 10L, 3, 5);

        when(rewardRepository.findByIdAndEnabledTrue(10L))
                .thenReturn(Optional.of(reward));

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(userProfile));

        assertThatThrownBy(() -> rewardRedemptionService.redeemReward(10L, 1L))
                .isInstanceOf(BusinessException.class);

        assertThat(userProfile.getCorePoints()).isEqualTo(10L);

        verify(rewardRepository).findByIdAndEnabledTrue(10L);
        verify(userProfileRepository).findByUserId(1L);
        verify(rewardRedemptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("redeemReward debe fallar si el usuario no tiene el nivel requerido")
    void redeemReward_shouldThrowWhenRequiredLevelNotReached() {
        Reward reward = reward(10L, 20L);
        reward.setRequiredLevel(5L);

        UserProfile userProfile = userProfile(1L, 100L, 3, 5);

        when(rewardRepository.findByIdAndEnabledTrue(10L))
                .thenReturn(Optional.of(reward));

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(userProfile));

        assertThatThrownBy(() -> rewardRedemptionService.redeemReward(10L, 1L))
                .isInstanceOf(BusinessException.class);

        assertThat(userProfile.getCorePoints()).isEqualTo(100L);

        verify(rewardRedemptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("redeemReward debe fallar si el usuario no tiene la racha requerida")
    void redeemReward_shouldThrowWhenRequiredStreakNotReached() {
        Reward reward = reward(10L, 20L);
        reward.setRequiredCurrentStreak(10L);

        UserProfile userProfile = userProfile(1L, 100L, 3, 5);

        when(rewardRepository.findByIdAndEnabledTrue(10L))
                .thenReturn(Optional.of(reward));

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(userProfile));

        assertThatThrownBy(() -> rewardRedemptionService.redeemReward(10L, 1L))
                .isInstanceOf(BusinessException.class);

        assertThat(userProfile.getCorePoints()).isEqualTo(100L);

        verify(rewardRedemptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("useRedemption debe marcar un canje REDEEMED como USED")
    void useRedemption_shouldMarkRedeemedRewardAsUsed() {
        Reward reward = reward(10L, 20L);
        UserProfile userProfile = userProfile(1L, 100L, 3, 5);

        RewardRedemption redemption = new RewardRedemption();
        redemption.setId(50L);
        redemption.setReward(reward);
        redemption.setUserProfile(userProfile);
        redemption.setStatus(RewardRedemptionStatus.REDEEMED);
        redemption.setCostPaid(20L);
        redemption.setRedeemedAt(LocalDateTime.now().minusMinutes(10));

        RewardRedemptionResponse response = mock(RewardRedemptionResponse.class);

        when(rewardRedemptionRepository.findByIdAndUserProfileUserId(50L, 1L))
                .thenReturn(Optional.of(redemption));

        when(rewardRedemptionRepository.save(any(RewardRedemption.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(rewardMapper.toRedemptionResponse(any(RewardRedemption.class)))
                .thenReturn(response);

        RewardRedemptionResponse result =
                rewardRedemptionService.useRedemption(50L, 1L);

        assertThat(result).isSameAs(response);
        assertThat(redemption.getStatus()).isEqualTo(RewardRedemptionStatus.USED);
        assertThat(redemption.getUsedAt()).isNotNull();

        verify(rewardRedemptionRepository).findByIdAndUserProfileUserId(50L, 1L);
        verify(rewardRedemptionRepository).save(redemption);
        verify(rewardMapper).toRedemptionResponse(redemption);
    }

    @Test
    @DisplayName("useRedemption debe fallar si el canje no existe o no pertenece al usuario")
    void useRedemption_shouldThrowWhenRedemptionNotFound() {
        when(rewardRedemptionRepository.findByIdAndUserProfileUserId(50L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> rewardRedemptionService.useRedemption(50L, 1L))
                .isInstanceOf(BusinessException.class);

        verify(rewardRedemptionRepository).findByIdAndUserProfileUserId(50L, 1L);
        verify(rewardRedemptionRepository, never()).save(any());
        verifyNoInteractions(rewardMapper);
    }

    @Test
    @DisplayName("useRedemption debe fallar si el canje no está en estado REDEEMED")
    void useRedemption_shouldThrowWhenRedemptionIsNotRedeemed() {
        Reward reward = reward(10L, 20L);
        UserProfile userProfile = userProfile(1L, 100L, 3, 5);

        RewardRedemption redemption = new RewardRedemption();
        redemption.setId(50L);
        redemption.setReward(reward);
        redemption.setUserProfile(userProfile);
        redemption.setStatus(RewardRedemptionStatus.USED);
        redemption.setCostPaid(20L);
        redemption.setRedeemedAt(LocalDateTime.now().minusMinutes(10));
        redemption.setUsedAt(LocalDateTime.now().minusMinutes(5));

        when(rewardRedemptionRepository.findByIdAndUserProfileUserId(50L, 1L))
                .thenReturn(Optional.of(redemption));

        assertThatThrownBy(() -> rewardRedemptionService.useRedemption(50L, 1L))
                .isInstanceOf(BusinessException.class);

        verify(rewardRedemptionRepository).findByIdAndUserProfileUserId(50L, 1L);
        verify(rewardRedemptionRepository, never()).save(any());
        verifyNoInteractions(rewardMapper);
    }

    @Test
    @DisplayName("redeemReward debe fallar si la recompensa no es repetible y ya fue canjeada")
    void redeemReward_shouldThrowWhenRewardIsNotRepeatableAndAlreadyRedeemed() {
        Reward reward = reward(10L, 20L);
        reward.setRepeatable(false);

        UserProfile userProfile = userProfile(1L, 100L, 3, 5);

        when(rewardRepository.findByIdAndEnabledTrue(10L))
                .thenReturn(Optional.of(reward));

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(userProfile));

        when(rewardRedemptionRepository.existsByRewardIdAndUserProfileUserIdAndStatusIn(
                eq(10L),
                eq(1L),
                anyCollection()
        )).thenReturn(true);

        assertThatThrownBy(() -> rewardRedemptionService.redeemReward(10L, 1L))
                .isInstanceOf(BusinessException.class);

        assertThat(userProfile.getCorePoints()).isEqualTo(100L);

        verify(rewardRedemptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("redeemReward debe permitir canjear recompensa no repetible si no existe canje previo válido")
    void redeemReward_shouldAllowNonRepeatableRewardWhenNoPreviousValidRedemptionExists() {
        Reward reward = reward(10L, 20L);
        reward.setRepeatable(false);

        UserProfile userProfile = userProfile(1L, 100L, 3, 5);

        when(rewardRepository.findByIdAndEnabledTrue(10L))
                .thenReturn(Optional.of(reward));

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(userProfile));

        when(rewardRedemptionRepository.existsByRewardIdAndUserProfileUserIdAndStatusIn(
                eq(10L),
                eq(1L),
                anyCollection()
        )).thenReturn(false);

        when(rewardRedemptionRepository.save(any(RewardRedemption.class)))
                .thenAnswer(invocation -> {
                    RewardRedemption redemption = invocation.getArgument(0);
                    redemption.setId(50L);
                    return redemption;
                });

        RewardRedeemResponse result =
                rewardRedemptionService.redeemReward(10L, 1L);

        assertThat(result.getRedemptionId()).isEqualTo(50L);
        assertThat(userProfile.getCorePoints()).isEqualTo(80L);

        verify(rewardRedemptionRepository).save(any(RewardRedemption.class));
    }

    @Test
    @DisplayName("redeemReward debe fallar si el cooldown sigue activo")
    void redeemReward_shouldThrowWhenCooldownIsActive() {
        Reward reward = reward(10L, 20L);
        reward.setCooldownDays(7);

        UserProfile userProfile = userProfile(1L, 100L, 3, 5);

        RewardRedemption previousRedemption = new RewardRedemption();
        previousRedemption.setId(40L);
        previousRedemption.setReward(reward);
        previousRedemption.setUserProfile(userProfile);
        previousRedemption.setStatus(RewardRedemptionStatus.USED);
        previousRedemption.setCostPaid(20L);
        previousRedemption.setRedeemedAt(LocalDateTime.now().minusDays(2));

        when(rewardRepository.findByIdAndEnabledTrue(10L))
                .thenReturn(Optional.of(reward));

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(userProfile));

        when(rewardRedemptionRepository
                .findTopByRewardIdAndUserProfileUserIdAndStatusInOrderByRedeemedAtDesc(
                        eq(10L),
                        eq(1L),
                        anyCollection()
                ))
                .thenReturn(Optional.of(previousRedemption));

        assertThatThrownBy(() -> rewardRedemptionService.redeemReward(10L, 1L))
                .isInstanceOf(BusinessException.class);

        assertThat(userProfile.getCorePoints()).isEqualTo(100L);

        verify(rewardRedemptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("redeemReward debe permitir canjear si el cooldown ya terminó")
    void redeemReward_shouldAllowRedeemWhenCooldownHasExpired() {
        Reward reward = reward(10L, 20L);
        reward.setCooldownDays(7);

        UserProfile userProfile = userProfile(1L, 100L, 3, 5);

        RewardRedemption previousRedemption = new RewardRedemption();
        previousRedemption.setId(40L);
        previousRedemption.setReward(reward);
        previousRedemption.setUserProfile(userProfile);
        previousRedemption.setStatus(RewardRedemptionStatus.USED);
        previousRedemption.setCostPaid(20L);
        previousRedemption.setRedeemedAt(LocalDateTime.now().minusDays(8));

        when(rewardRepository.findByIdAndEnabledTrue(10L))
                .thenReturn(Optional.of(reward));

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(userProfile));

        when(rewardRedemptionRepository
                .findTopByRewardIdAndUserProfileUserIdAndStatusInOrderByRedeemedAtDesc(
                        eq(10L),
                        eq(1L),
                        anyCollection()
                ))
                .thenReturn(Optional.of(previousRedemption));

        when(rewardRedemptionRepository.save(any(RewardRedemption.class)))
                .thenAnswer(invocation -> {
                    RewardRedemption redemption = invocation.getArgument(0);
                    redemption.setId(50L);
                    return redemption;
                });

        RewardRedeemResponse result =
                rewardRedemptionService.redeemReward(10L, 1L);

        assertThat(result.getRedemptionId()).isEqualTo(50L);
        assertThat(userProfile.getCorePoints()).isEqualTo(80L);

        verify(rewardRedemptionRepository).save(any(RewardRedemption.class));
    }

    private Reward reward(Long id, Long costCorePoints) {
        Reward reward = new Reward();
        reward.setId(id);
        reward.setName("2 horas de gaming");
        reward.setDescription("Tiempo para jugar videojuegos");
        reward.setType(RewardType.REAL);
        reward.setCategory(RewardCategory.GAMING);
        reward.setCostCorePoints(costCorePoints);
        reward.setEnabled(true);
        reward.setRepeatable(true);
        return reward;
    }

    private UserProfile userProfile(
            Long userId,
            Long corePoints,
            Integer level,
            Integer currentStreak
    ) {
        User user = User.builder()
                .id(userId)
                .username("david")
                .email("david@test.com")
                .password("encoded")
                .role("USER")
                .enabled(true)
                .build();

        return UserProfile.builder()
                .id(1L)
                .user(user)
                .displayName("David")
                .dailyTaskGoal(3)
                .totalXp(250L)
                .level(level)
                .corePoints(corePoints)
                .currentStreak(currentStreak)
                .bestStreak(currentStreak)
                .build();
    }
}