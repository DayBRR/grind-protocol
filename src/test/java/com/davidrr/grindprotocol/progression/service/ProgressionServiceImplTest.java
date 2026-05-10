package com.davidrr.grindprotocol.progression.service;

import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.progression.service.impl.ProgressionServiceImpl;
import com.davidrr.grindprotocol.task.model.Task;
import com.davidrr.grindprotocol.task.model.TaskCompletion;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import com.davidrr.grindprotocol.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressionServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private ProgressionServiceImpl progressionService;

    private User user;
    private UserProfile profile;
    private Task task;

    @BeforeEach
    void setUp() {
        user = TestDataFactory.user();
        user.setId(1L);

        profile = TestDataFactory.userProfile(user);
        task = TestDataFactory.task(10L, user, true);
    }

    @Test
    @DisplayName("Debe sumar XP, Core Points y recalcular el nivel al completar una tarea")
    void applyTaskCompletionProgress_shouldAddXpCorePointsAndRecalculateLevel() {
        profile.setTotalXp(100L);
        profile.setLevel(2);
        profile.setCorePoints(10L);

        TaskCompletion completion = TestDataFactory.taskCompletion(100L, task, user);
        completion.setAwardedXp(50);
        completion.setAwardedCorePoints(5);

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);

        when(userProfileRepository.save(profileCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        progressionService.applyTaskCompletionProgress(1L, completion);

        UserProfile savedProfile = profileCaptor.getValue();

        assertThat(savedProfile.getTotalXp()).isEqualTo(150L);
        assertThat(savedProfile.getLevel()).isEqualTo(2);
        assertThat(savedProfile.getCorePoints()).isEqualTo(15L);

        verify(userProfileRepository).findByUserId(1L);
        verify(userProfileRepository).save(profile);
    }

    @Test
    @DisplayName("Debe fallar si no existe UserProfile para el usuario")
    void applyTaskCompletionProgress_shouldThrowWhenUserProfileNotFound() {
        TaskCompletion completion = TestDataFactory.taskCompletion(100L, task, user);

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> progressionService.applyTaskCompletionProgress(1L, completion))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userProfileRepository).findByUserId(1L);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe permitir sumar cero XP y cero Core Points sin cambiar nivel")
    void applyTaskCompletionProgress_shouldAllowZeroProgress() {
        profile.setTotalXp(100L);
        profile.setLevel(2);
        profile.setCorePoints(10L);

        TaskCompletion completion = TestDataFactory.taskCompletion(100L, task, user);
        completion.setAwardedXp(0);
        completion.setAwardedCorePoints(0);

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        progressionService.applyTaskCompletionProgress(1L, completion);

        assertThat(profile.getTotalXp()).isEqualTo(100L);
        assertThat(profile.getLevel()).isEqualTo(2);
        assertThat(profile.getCorePoints()).isEqualTo(10L);

        verify(userProfileRepository).save(profile);
    }

    @Test
    @DisplayName("Debe subir de nivel cuando la XP total alcanza el siguiente umbral")
    void shouldIncreaseLevelWhenXpThresholdIsReached() {
        profile.setTotalXp(90L);
        profile.setLevel(1);
        profile.setCorePoints(0L);

        TaskCompletion completion = TaskCompletion.builder()
                .awardedXp(20)
                .awardedCorePoints(2)
                .build();

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(profile));

        progressionService.applyTaskCompletionProgress(1L, completion);

        assertThat(profile.getTotalXp()).isEqualTo(110L);
        assertThat(profile.getLevel()).isEqualTo(2);
        assertThat(profile.getCorePoints()).isEqualTo(2L);

        verify(userProfileRepository).save(profile);
    }

    @Test
    @DisplayName("Debe mantener el mismo nivel cuando no se alcanza el siguiente umbral")
    void shouldKeepSameLevelWhenThresholdNotReached() {
        profile.setTotalXp(50L);
        profile.setLevel(1);
        profile.setCorePoints(0L);

        TaskCompletion completion = TaskCompletion.builder()
                .awardedXp(20)
                .awardedCorePoints(2)
                .build();

        when(userProfileRepository.findByUserId(1L))
                .thenReturn(Optional.of(profile));

        progressionService.applyTaskCompletionProgress(1L, completion);

        assertThat(profile.getTotalXp()).isEqualTo(70L);
        assertThat(profile.getLevel()).isEqualTo(1);
        assertThat(profile.getCorePoints()).isEqualTo(2L);

        verify(userProfileRepository).save(profile);
    }
}