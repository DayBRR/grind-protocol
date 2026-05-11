package com.davidrr.grindprotocol.progression.service;

import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.progression.dto.ProgressionSummaryResponse;
import com.davidrr.grindprotocol.progression.service.calculator.LevelCalculator;
import com.davidrr.grindprotocol.progression.service.impl.ProgressionSummaryServiceImpl;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import com.davidrr.grindprotocol.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressionSummaryServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private LevelCalculator levelCalculator;

    @InjectMocks
    private ProgressionSummaryServiceImpl progressionSummaryService;

    private User user;
    private UserProfile profile;

    @BeforeEach
    void setUp() {
        user = TestDataFactory.user();
        user.setId(1L);

        profile = TestDataFactory.userProfile(user);
    }

    @Test
    @DisplayName("Debe devolver el resumen de progreso del usuario")
    void getSummary_shouldReturnProgressionSummary() {
        profile.setTotalXp(250L);
        profile.setLevel(3);
        profile.setCorePoints(25L);
        profile.setCurrentStreak(4);
        profile.setBestStreak(7);

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(profile));

        when(levelCalculator.getXpForCurrentLevel(250L)).thenReturn(200L);
        when(levelCalculator.getXpForNextLevel(250L)).thenReturn(300L);
        when(levelCalculator.getXpProgressInCurrentLevel(250L)).thenReturn(50L);
        when(levelCalculator.getXpRemainingForNextLevel(250L)).thenReturn(50L);

        ProgressionSummaryResponse response = progressionSummaryService.getSummary(1L);

        assertThat(response.getTotalXp()).isEqualTo(250L);
        assertThat(response.getLevel()).isEqualTo(3);
        assertThat(response.getXpForCurrentLevel()).isEqualTo(200L);
        assertThat(response.getXpForNextLevel()).isEqualTo(300L);
        assertThat(response.getXpProgressInCurrentLevel()).isEqualTo(50L);
        assertThat(response.getXpRemainingForNextLevel()).isEqualTo(50L);
        assertThat(response.getCorePoints()).isEqualTo(25L);
        assertThat(response.getCurrentStreak()).isEqualTo(4);
        assertThat(response.getBestStreak()).isEqualTo(7);

        verify(userProfileRepository).findByUserId(1L);
        verify(levelCalculator).getXpForCurrentLevel(250L);
        verify(levelCalculator).getXpForNextLevel(250L);
        verify(levelCalculator).getXpProgressInCurrentLevel(250L);
        verify(levelCalculator).getXpRemainingForNextLevel(250L);
    }

    @Test
    @DisplayName("Debe fallar si no existe UserProfile para el usuario")
    void getSummary_shouldThrowWhenUserProfileNotFound() {
        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressionSummaryService.getSummary(1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userProfileRepository).findByUserId(1L);
        verifyNoInteractions(levelCalculator);
    }
}