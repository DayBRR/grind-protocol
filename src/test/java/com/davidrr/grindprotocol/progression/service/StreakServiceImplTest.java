package com.davidrr.grindprotocol.progression.service;

import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.progression.service.impl.StreakServiceImpl;
import com.davidrr.grindprotocol.task.model.DailyProgress;
import com.davidrr.grindprotocol.task.repository.DailyProgressRepository;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreakServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate TODAY = LocalDate.of(2026, 4, 18);
    private static final LocalDate YESTERDAY = TODAY.minusDays(1);

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private DailyProgressRepository dailyProgressRepository;

    @InjectMocks
    private StreakServiceImpl streakService;

    private User user;
    private UserProfile profile;
    private DailyProgress progress;

    @BeforeEach
    void setUp() {
        user = TestDataFactory.user();
        profile = TestDataFactory.userProfile(user);
        progress = TestDataFactory.dailyProgress(user);
        progress.setProgressDate(TODAY);
    }

    @Test
    @DisplayName("Debe iniciar racha a 1 cuando el día califica y no había racha previa")
    void finalizeDay_shouldStartStreakWhenDayQualifiedAndNoPreviousEvaluation() {
        progress.setDayQualified(true);

        when(userProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile));
        when(dailyProgressRepository.findByUserIdAndProgressDate(USER_ID, TODAY)).thenReturn(Optional.of(progress));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        streakService.finalizeDay(USER_ID, TODAY);

        assertThat(profile.getCurrentStreak()).isEqualTo(1);
        assertThat(profile.getBestStreak()).isEqualTo(1);
        assertThat(profile.getLastEvaluatedDate()).isEqualTo(TODAY);

        verify(userProfileRepository).save(profile);
    }

    @Test
    @DisplayName("Debe incrementar racha cuando hoy califica y ayer fue la última fecha evaluada")
    void finalizeDay_shouldIncrementStreakWhenYesterdayWasEvaluatedAndTodayQualified() {
        profile.setCurrentStreak(3);
        profile.setBestStreak(5);
        profile.setLastEvaluatedDate(YESTERDAY);
        progress.setDayQualified(true);

        when(userProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile));
        when(dailyProgressRepository.findByUserIdAndProgressDate(USER_ID, TODAY)).thenReturn(Optional.of(progress));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        streakService.finalizeDay(USER_ID, TODAY);

        assertThat(profile.getCurrentStreak()).isEqualTo(4);
        assertThat(profile.getBestStreak()).isEqualTo(5);
        assertThat(profile.getLastEvaluatedDate()).isEqualTo(TODAY);
    }

    @Test
    @DisplayName("Debe actualizar bestStreak cuando la racha actual supera el récord")
    void finalizeDay_shouldUpdateBestStreakWhenCurrentStreakBeatsRecord() {
        profile.setCurrentStreak(5);
        profile.setBestStreak(5);
        profile.setLastEvaluatedDate(YESTERDAY);
        progress.setDayQualified(true);

        when(userProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile));
        when(dailyProgressRepository.findByUserIdAndProgressDate(USER_ID, TODAY)).thenReturn(Optional.of(progress));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        streakService.finalizeDay(USER_ID, TODAY);

        assertThat(profile.getCurrentStreak()).isEqualTo(6);
        assertThat(profile.getBestStreak()).isEqualTo(6);
    }

    @Test
    @DisplayName("Debe reiniciar la racha a 1 si hoy califica tras una ruptura")
    void finalizeDay_shouldRestartStreakWhenPreviousEvaluatedDateIsNotYesterday() {
        profile.setCurrentStreak(7);
        profile.setBestStreak(10);
        profile.setLastEvaluatedDate(TODAY.minusDays(3));
        progress.setDayQualified(true);

        when(userProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile));
        when(dailyProgressRepository.findByUserIdAndProgressDate(USER_ID, TODAY)).thenReturn(Optional.of(progress));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        streakService.finalizeDay(USER_ID, TODAY);

        assertThat(profile.getCurrentStreak()).isEqualTo(1);
        assertThat(profile.getBestStreak()).isEqualTo(10);
        assertThat(profile.getLastEvaluatedDate()).isEqualTo(TODAY);
    }

    @Test
    @DisplayName("Debe poner currentStreak a 0 cuando el día no califica")
    void finalizeDay_shouldResetCurrentStreakWhenDayDoesNotQualify() {
        profile.setCurrentStreak(4);
        profile.setBestStreak(8);
        profile.setLastEvaluatedDate(YESTERDAY);
        progress.setDayQualified(false);

        when(userProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile));
        when(dailyProgressRepository.findByUserIdAndProgressDate(USER_ID, TODAY)).thenReturn(Optional.of(progress));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        streakService.finalizeDay(USER_ID, TODAY);

        assertThat(profile.getCurrentStreak()).isZero();
        assertThat(profile.getBestStreak()).isEqualTo(8);
        assertThat(profile.getLastEvaluatedDate()).isEqualTo(TODAY);
    }

    @Test
    @DisplayName("No debe evaluar dos veces el mismo día")
    void finalizeDay_shouldNotEvaluateSameDateTwice() {
        profile.setCurrentStreak(4);
        profile.setBestStreak(6);
        profile.setLastEvaluatedDate(TODAY);
        progress.setDayQualified(true);

        when(userProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile));
        when(dailyProgressRepository.findByUserIdAndProgressDate(USER_ID, TODAY)).thenReturn(Optional.of(progress));

        streakService.finalizeDay(USER_ID, TODAY);

        assertThat(profile.getCurrentStreak()).isEqualTo(4);
        assertThat(profile.getBestStreak()).isEqualTo(6);
        assertThat(profile.getLastEvaluatedDate()).isEqualTo(TODAY);

        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("Debe fallar si no existe UserProfile")
    void finalizeDay_shouldThrowWhenUserProfileDoesNotExist() {
        when(userProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> streakService.finalizeDay(USER_ID, TODAY))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(dailyProgressRepository, never()).findByUserIdAndProgressDate(anyLong(), any(LocalDate.class));
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe fallar si no existe DailyProgress para la fecha")
    void finalizeDay_shouldThrowWhenDailyProgressDoesNotExist() {
        when(userProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile));
        when(dailyProgressRepository.findByUserIdAndProgressDate(USER_ID, TODAY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> streakService.finalizeDay(USER_ID, TODAY))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userProfileRepository, never()).save(any());
    }
}
