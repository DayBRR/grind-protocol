package com.davidrr.grindprotocol.progression.controller;

import com.davidrr.grindprotocol.activity.dto.WeeklyProgressionSummaryResponse;
import com.davidrr.grindprotocol.activity.service.UserActivityEventService;
import com.davidrr.grindprotocol.progression.dto.ProgressionSummaryResponse;
import com.davidrr.grindprotocol.progression.service.ProgressionSummaryService;
import com.davidrr.grindprotocol.progression.service.StreakService;
import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import com.davidrr.grindprotocol.utils.TestAuthenticatedUserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressionControllerTest {

    @Mock
    private StreakService streakService;

    @Mock
    private ProgressionSummaryService progressionSummaryService;

    @Mock
    private UserActivityEventService userActivityEventService;

    @InjectMocks
    private ProgressionController progressionController;

    @Test
    @DisplayName("finalizeDay debe delegar en StreakService usando la fecha recibida")
    void finalizeDay_shouldDelegateToStreakServiceWithProvidedDate() {
        AuthenticatedUser currentUser = TestAuthenticatedUserFactory.defaultUser();
        LocalDate date = LocalDate.of(2026, 4, 18);

        progressionController.finalizeDay(currentUser, date);

        verify(streakService).finalizeDay(1L, date);
    }

    @Test
    @DisplayName("finalizeDay debe usar LocalDate.now cuando no se informa fecha")
    void finalizeDay_shouldUseCurrentDateWhenDateIsNull() {
        AuthenticatedUser currentUser = TestAuthenticatedUserFactory.defaultUser();
        LocalDate today = LocalDate.now();

        progressionController.finalizeDay(currentUser, null);

        verify(streakService).finalizeDay(1L, today);
    }

    @Test
    @DisplayName("getSummary debe delegar en ProgressionSummaryService")
    void getSummary_shouldDelegateToProgressionSummaryService() {
        AuthenticatedUser currentUser = TestAuthenticatedUserFactory.defaultUser();
        ProgressionSummaryResponse response = ProgressionSummaryResponse.builder()
                .totalXp(250L)
                .level(3)
                .corePoints(25L)
                .currentStreak(4)
                .bestStreak(7)
                .build();

        when(progressionSummaryService.getSummary(1L)).thenReturn(response);

        ProgressionSummaryResponse result = progressionController.getSummary(currentUser);

        assertThat(result).isSameAs(response);
        verify(progressionSummaryService).getSummary(1L);
    }

    @Test
    @DisplayName("getWeeklySummary debe delegar en UserActivityEventService con la fecha recibida")
    void getWeeklySummary_shouldDelegateToUserActivityEventService() {
        AuthenticatedUser currentUser = TestAuthenticatedUserFactory.defaultUser();
        LocalDate date = LocalDate.of(2026, 6, 4);
        WeeklyProgressionSummaryResponse response = WeeklyProgressionSummaryResponse.builder()
                .weekStart(LocalDate.of(2026, 6, 1))
                .weekEnd(LocalDate.of(2026, 6, 7))
                .totalXp(120)
                .previousWeekTotalXp(80)
                .days(List.of())
                .build();

        when(userActivityEventService.getWeeklySummary(1L, date)).thenReturn(response);

        WeeklyProgressionSummaryResponse result = progressionController.getWeeklySummary(currentUser, date);

        assertThat(result).isSameAs(response);
        verify(userActivityEventService).getWeeklySummary(1L, date);
    }
}
