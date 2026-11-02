package com.davidrr.grindprotocol.progression.service;

import com.davidrr.grindprotocol.achievement.service.AchievementEvaluationService;
import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.progression.service.impl.StreakServiceImpl;
import com.davidrr.grindprotocol.progression.time.DayCalendar;
import com.davidrr.grindprotocol.task.model.DailyProgress;
import com.davidrr.grindprotocol.task.repository.DailyProgressRepository;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import com.davidrr.grindprotocol.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StreakServiceImplTest {
    private static final LocalDate DAY = LocalDate.of(2026, 4, 18);
    @Mock UserProfileRepository userProfileRepository;
    @Mock DailyProgressRepository dailyProgressRepository;
    @Mock AchievementEvaluationService achievementEvaluationService;
    @Spy DayCalendar calendar = new DayCalendar(Clock.fixed(Instant.parse("2026-04-19T12:00:00Z"), ZoneOffset.UTC));
    @InjectMocks StreakServiceImpl service;
    UserProfile profile;

    @BeforeEach void setUp() {
        profile = TestDataFactory.userProfile(TestDataFactory.user());
        when(userProfileRepository.findLockedByUserId(1L)).thenReturn(Optional.of(profile));
    }
    private DailyProgress snapshot(LocalDate date, boolean qualified) {
        DailyProgress p = TestDataFactory.dailyProgress(profile.getUser());
        p.setProgressDate(date); p.setDayQualified(qualified); return p;
    }
    private void snapshots(DailyProgress... values) {
        when(dailyProgressRepository.findByUserIdAndProgressDateBetweenOrderByProgressDateAsc(eq(1L), any(), eq(DAY)))
                .thenReturn(List.of(values));
    }
    @Test void startsStreakFromClosedSnapshot() {
        snapshots(snapshot(DAY, true)); service.closeElapsedDays(1L);
        assertThat(profile.getCurrentStreak()).isEqualTo(1);
        assertThat(profile.getBestStreak()).isEqualTo(1);
        assertThat(profile.getLastEvaluatedDate()).isEqualTo(DAY);
        verify(achievementEvaluationService).evaluateAchievements(1L);
    }
    @Test void incrementsAndPreservesBest() {
        profile.setLastEvaluatedDate(DAY.minusDays(1)); profile.setCurrentStreak(3); profile.setBestStreak(8);
        snapshots(snapshot(DAY, true)); service.finalizeDay(1L, DAY);
        assertThat(profile.getCurrentStreak()).isEqualTo(4); assertThat(profile.getBestStreak()).isEqualTo(8);
    }
    @Test void increasesBest() {
        profile.setLastEvaluatedDate(DAY.minusDays(1)); profile.setCurrentStreak(3); profile.setBestStreak(3);
        snapshots(snapshot(DAY, true)); service.finalizeDay(1L, DAY);
        assertThat(profile.getBestStreak()).isEqualTo(4);
    }
    @Test void breaksStreakWithoutLosingRecord() {
        profile.setLastEvaluatedDate(DAY.minusDays(1)); profile.setCurrentStreak(3); profile.setBestStreak(8);
        snapshots(snapshot(DAY, false)); service.finalizeDay(1L, DAY);
        assertThat(profile.getCurrentStreak()).isZero(); assertThat(profile.getBestStreak()).isEqualTo(8);
        assertThat(profile.getLastEvaluatedDate()).isEqualTo(DAY);
    }
    @Test void catchesUpChronologicallyAndBreaksAtMissingDay() {
        snapshots(snapshot(DAY.minusDays(3), true), snapshot(DAY.minusDays(2), true), snapshot(DAY, true));
        service.finalizeDay(1L, DAY);
        assertThat(profile.getCurrentStreak()).isEqualTo(1); assertThat(profile.getBestStreak()).isEqualTo(2);
    }
    @Test void persistsAnInactiveDayAsUnqualified() {
        profile.setLastEvaluatedDate(DAY.minusDays(1)); profile.setCurrentStreak(3); profile.setBestStreak(8);
        when(dailyProgressRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        service.closeElapsedDays(1L);
        ArgumentCaptor<DailyProgress> captured = ArgumentCaptor.forClass(DailyProgress.class);
        verify(dailyProgressRepository).save(captured.capture());
        assertThat(captured.getValue().isDayQualified()).isFalse();
        assertThat(profile.getCurrentStreak()).isZero(); assertThat(profile.getBestStreak()).isEqualTo(8);
    }
    @Test void repeatedAndOutOfOrderFinalizationDoNothing() {
        profile.setLastEvaluatedDate(DAY); profile.setCurrentStreak(4); profile.setBestStreak(6);
        service.finalizeDay(1L, DAY); service.finalizeDay(1L, DAY.minusDays(1));
        assertThat(profile.getCurrentStreak()).isEqualTo(4);
        verifyNoInteractions(dailyProgressRepository, achievementEvaluationService);
        verify(userProfileRepository, never()).save(any());
    }
    @Test void rejectsOpenAndFutureDay() {
        assertThatThrownBy(() -> service.finalizeDay(1L, DAY.plusDays(1))).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.finalizeDay(1L, DAY.plusDays(2))).isInstanceOf(BusinessException.class);
        verifyNoInteractions(dailyProgressRepository);
    }
    @Test void missingProfileFails() {
        when(userProfileRepository.findLockedByUserId(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.closeElapsedDays(1L)).isInstanceOf(ResourceNotFoundException.class);
    }
    @Test void repairsLegacyPrematureEvaluationUsingOnlyClosedHistory() {
        profile.setLastEvaluatedDate(DAY.plusDays(1)); profile.setCurrentStreak(2); profile.setBestStreak(2);
        snapshots(snapshot(DAY, true)); service.closeElapsedDays(1L);
        assertThat(profile.getLastEvaluatedDate()).isEqualTo(DAY);
        assertThat(profile.getCurrentStreak()).isEqualTo(1); assertThat(profile.getBestStreak()).isEqualTo(1);
    }
}
