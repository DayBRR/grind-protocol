package com.davidrr.grindprotocol.progression.service.impl;

import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.progression.service.StreakService;
import com.davidrr.grindprotocol.progression.time.DayCalendar;
import com.davidrr.grindprotocol.achievement.service.AchievementEvaluationService;
import com.davidrr.grindprotocol.task.model.DailyProgress;
import com.davidrr.grindprotocol.task.repository.DailyProgressRepository;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StreakServiceImpl implements StreakService {
    private final UserProfileRepository userProfileRepository;
    private final DailyProgressRepository dailyProgressRepository;
    private final DayCalendar calendar;
    private final AchievementEvaluationService achievementEvaluationService;

    @Override
    public void closeElapsedDays(Long userId) {
        finalizeDay(userId, null);
    }

    @Override
    public void finalizeDay(Long userId, LocalDate date) {
        // Global lock order: profile -> daily progress -> claim instances.
        UserProfile profile = userProfileRepository.findLockedByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.UserProfile.NOT_FOUND, "User profile not found for user id: " + userId));
        LocalDate today = calendar.today();
        LocalDate target = date == null ? today.minusDays(1) : date;
        if (!target.isBefore(today)) {
            throw new BusinessException(ErrorCodes.DailyProgress.DAY_NOT_CLOSED, "Only elapsed days can be finalized");
        }

        // Repair a legacy evaluation of the still-open day using closed snapshots only.
        boolean repaired = profile.getLastEvaluatedDate() != null && !profile.getLastEvaluatedDate().isBefore(today);
        if (repaired) {
            profile.setCurrentStreak(0);
            profile.setBestStreak(0);
            profile.setLastEvaluatedDate(null);
            applySnapshots(profile, dailyProgressRepository
                    .findByUserIdAndProgressDateBetweenOrderByProgressDateAsc(userId, LocalDate.of(1970, 1, 1), today.minusDays(1)));
        }

        LocalDate last = profile.getLastEvaluatedDate();
        if (last != null && !target.isAfter(last)) {
            if (repaired) {
                userProfileRepository.save(profile);
                achievementEvaluationService.evaluateAchievements(userId);
            }
            return; // Monotonic watermark: repeated AND out-of-order requests are harmless.
        }
        LocalDate start = last == null ? LocalDate.of(1970, 1, 1) : last.plusDays(1);
        List<DailyProgress> snapshots = dailyProgressRepository
                .findByUserIdAndProgressDateBetweenOrderByProgressDateAsc(userId, start, target);
        // Audit timestamps predate the business-zone setting. A real daily snapshot
        // takes precedence over their JVM-local registration date at zone boundaries.
        if (last == null && snapshots.isEmpty() && profile.getCreatedAt() != null
                && target.isBefore(profile.getCreatedAt().toLocalDate())) {
            return;
        }
        applySnapshots(profile, snapshots);

        if (!target.equals(profile.getLastEvaluatedDate())) {
            // No activity snapshot on the last elapsed day: its minimum was not met.
            DailyProgress empty = dailyProgressRepository.findByUserIdAndProgressDate(userId, target)
                    .orElseGet(() -> dailyProgressRepository.save(DailyProgress.builder()
                            .user(profile.getUser()).progressDate(target).requiredTaskCount(3)
                            .completedValidTaskCount(0).mandatoryTasksRequired(0).mandatoryTasksCompleted(0)
                            .dayQualified(false).evaluatedAt(calendar.now()).build()));
            applySnapshot(profile, empty);
        }
        userProfileRepository.save(profile);
        achievementEvaluationService.evaluateAchievements(userId);
    }

    private void applySnapshots(UserProfile profile, List<DailyProgress> snapshots) {
        snapshots.forEach(progress -> applySnapshot(profile, progress));
    }

    private void applySnapshot(UserProfile profile, DailyProgress progress) {
        LocalDate last = profile.getLastEvaluatedDate();
        int streak = progress.isDayQualified()
                ? last != null && last.plusDays(1).equals(progress.getProgressDate())
                    ? profile.getCurrentStreak() + 1 : 1
                : 0;
        profile.setCurrentStreak(streak);
        profile.setBestStreak(Math.max(profile.getBestStreak(), streak));
        profile.setLastEvaluatedDate(progress.getProgressDate());
    }
}
