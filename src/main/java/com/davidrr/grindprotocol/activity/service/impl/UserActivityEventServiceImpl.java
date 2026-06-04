package com.davidrr.grindprotocol.activity.service.impl;

import com.davidrr.grindprotocol.activity.dto.ActivityItemResponse;
import com.davidrr.grindprotocol.activity.dto.DailyProgressionSummaryResponse;
import com.davidrr.grindprotocol.activity.dto.RecentActivityResponse;
import com.davidrr.grindprotocol.activity.dto.WeeklyProgressionSummaryResponse;
import com.davidrr.grindprotocol.activity.enums.ActivitySourceType;
import com.davidrr.grindprotocol.activity.enums.ActivityType;
import com.davidrr.grindprotocol.activity.model.UserActivityEvent;
import com.davidrr.grindprotocol.activity.repository.UserActivityEventRepository;
import com.davidrr.grindprotocol.activity.service.UserActivityEventService;
import com.davidrr.grindprotocol.achievement.model.Achievement;
import com.davidrr.grindprotocol.achievement.model.UserAchievement;
import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.quest.model.Quest;
import com.davidrr.grindprotocol.quest.model.UserQuest;
import com.davidrr.grindprotocol.reward.model.Reward;
import com.davidrr.grindprotocol.reward.model.RewardRedemption;
import com.davidrr.grindprotocol.task.model.Task;
import com.davidrr.grindprotocol.task.model.TaskCompletion;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActivityEventServiceImpl implements UserActivityEventService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    private final UserActivityEventRepository userActivityEventRepository;
    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void recordTaskCompleted(TaskCompletion completion) {
        Task task = completion.getTask();
        UserProfile userProfile = findUserProfile(completion.getUser().getId());

        recordEvent(
                userProfile,
                ActivityType.TASK_COMPLETED,
                ActivitySourceType.TASK_COMPLETION,
                completion.getId(),
                "Completed task: " + task.getTitle(),
                completion.getNotes(),
                completion.getAwardedXp(),
                completion.getAwardedCorePoints(),
                completion.getCompletedAt(),
                Map.of(
                        "taskId", task.getId(),
                        "taskTitle", task.getTitle(),
                        "category", task.getCategory().name(),
                        "difficulty", task.getDifficulty().name(),
                        "completionIndexForDay", completion.getCompletionIndexForDay()
                )
        );
    }

    @Override
    @Transactional
    public void recordQuestClaimed(UserQuest userQuest) {
        Quest quest = userQuest.getQuest();

        recordEvent(
                userQuest.getUserProfile(),
                ActivityType.QUEST_CLAIMED,
                ActivitySourceType.QUEST,
                quest.getId(),
                "Claimed quest: " + quest.getName(),
                quest.getDescription(),
                safeLongToInt(quest.getXpReward()),
                safeLongToInt(quest.getCorePointsReward()),
                userQuest.getClaimedAt(),
                Map.of(
                        "questId", quest.getId(),
                        "questCode", quest.getCode(),
                        "frequency", quest.getFrequency().name(),
                        "periodStart", userQuest.getPeriodStart().toString(),
                        "periodEnd", userQuest.getPeriodEnd().toString()
                )
        );
    }

    @Override
    @Transactional
    public void recordAchievementClaimed(UserAchievement userAchievement) {
        Achievement achievement = userAchievement.getAchievement();

        recordEvent(
                userAchievement.getUserProfile(),
                ActivityType.ACHIEVEMENT_CLAIMED,
                ActivitySourceType.ACHIEVEMENT,
                achievement.getId(),
                "Claimed achievement: " + achievement.getName(),
                achievement.getDescription(),
                safeLongToInt(achievement.getXpReward()),
                safeLongToInt(achievement.getCorePointsReward()),
                userAchievement.getClaimedAt(),
                Map.of(
                        "achievementId", achievement.getId(),
                        "achievementCode", achievement.getCode(),
                        "achievementType", achievement.getType().name()
                )
        );
    }

    @Override
    @Transactional
    public void recordRewardRedeemed(RewardRedemption redemption) {
        Reward reward = redemption.getReward();

        recordEvent(
                redemption.getUserProfile(),
                ActivityType.REWARD_REDEEMED,
                ActivitySourceType.REWARD_REDEMPTION,
                redemption.getId(),
                "Redeemed reward: " + reward.getName(),
                reward.getDescription(),
                0,
                -safeLongToInt(redemption.getCostPaid()),
                redemption.getRedeemedAt(),
                Map.of(
                        "rewardId", reward.getId(),
                        "rewardName", reward.getName(),
                        "rewardType", reward.getType().name(),
                        "costPaid", redemption.getCostPaid()
                )
        );
    }

    @Override
    @Transactional
    public void recordRewardUsed(RewardRedemption redemption) {
        Reward reward = redemption.getReward();

        recordEvent(
                redemption.getUserProfile(),
                ActivityType.REWARD_USED,
                ActivitySourceType.REWARD_REDEMPTION,
                redemption.getId(),
                "Used reward: " + reward.getName(),
                reward.getDescription(),
                0,
                0,
                redemption.getUsedAt(),
                Map.of(
                        "rewardId", reward.getId(),
                        "rewardName", reward.getName(),
                        "rewardType", reward.getType().name()
                )
        );
    }

    @Override
    public RecentActivityResponse getRecentActivity(Long userId, int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit <= 0 ? DEFAULT_LIMIT : limit, MAX_LIMIT));

        List<ActivityItemResponse> items = userActivityEventRepository
                .findByUserProfileUserIdOrderByOccurredAtDesc(userId, PageRequest.of(0, resolvedLimit))
                .stream()
                .map(this::toActivityItemResponse)
                .toList();

        return RecentActivityResponse.builder()
                .items(items)
                .build();
    }

    @Override
    public WeeklyProgressionSummaryResponse getWeeklySummary(Long userId, LocalDate referenceDate) {
        LocalDate targetDate = referenceDate != null ? referenceDate : LocalDate.now();
        LocalDate weekStart = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        LocalDate previousWeekStart = weekStart.minusWeeks(1);
        LocalDate previousWeekEnd = weekEnd.minusWeeks(1);

        List<UserActivityEvent> currentWeekEvents = findEventsBetween(userId, weekStart, weekEnd);
        List<UserActivityEvent> previousWeekEvents = findEventsBetween(userId, previousWeekStart, previousWeekEnd);

        int totalXp = sumXp(currentWeekEvents);
        int previousWeekTotalXp = sumXp(previousWeekEvents);

        Map<LocalDate, List<UserActivityEvent>> eventsByDate = currentWeekEvents.stream()
                .collect(Collectors.groupingBy(event -> event.getOccurredAt().toLocalDate()));

        List<DailyProgressionSummaryResponse> days = weekStart.datesUntil(weekEnd.plusDays(1))
                .map(date -> toDailySummary(date, eventsByDate.getOrDefault(date, List.of())))
                .toList();

        return WeeklyProgressionSummaryResponse.builder()
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .totalXp(totalXp)
                .previousWeekTotalXp(previousWeekTotalXp)
                .deltaPercent(calculateDeltaPercent(totalXp, previousWeekTotalXp))
                .days(days)
                .build();
    }

    private void recordEvent(
            UserProfile userProfile,
            ActivityType type,
            ActivitySourceType sourceType,
            Long sourceId,
            String title,
            String description,
            Integer xpDelta,
            Integer corePointsDelta,
            LocalDateTime occurredAt,
            Map<String, Object> metadata
    ) {
        UserActivityEvent event = UserActivityEvent.builder()
                .userProfile(userProfile)
                .type(type)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .title(title)
                .description(description)
                .xpDelta(xpDelta != null ? xpDelta : 0)
                .corePointsDelta(corePointsDelta != null ? corePointsDelta : 0)
                .occurredAt(occurredAt != null ? occurredAt : LocalDateTime.now())
                .metadataJson(toMetadataJson(metadata))
                .build();

        userActivityEventRepository.save(event);
    }

    private UserProfile findUserProfile(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.UserProfile.NOT_FOUND,
                        "User profile not found for user id: " + userId
                ));
    }

    private List<UserActivityEvent> findEventsBetween(Long userId, LocalDate startDate, LocalDate endDate) {
        return userActivityEventRepository.findByUserProfileUserIdAndOccurredAtBetweenOrderByOccurredAtAsc(
                userId,
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay().minusNanos(1)
        );
    }

    private DailyProgressionSummaryResponse toDailySummary(LocalDate date, List<UserActivityEvent> events) {
        Map<ActivityType, Long> counts = new EnumMap<>(ActivityType.class);
        for (ActivityType type : ActivityType.values()) {
            counts.put(type, 0L);
        }

        events.forEach(event -> counts.computeIfPresent(event.getType(), (key, current) -> current + 1));

        return DailyProgressionSummaryResponse.builder()
                .date(date)
                .dayOfWeek(date.getDayOfWeek().name())
                .xpEarned(sumXp(events))
                .corePointsEarned(events.stream()
                        .mapToInt(event -> Math.max(0, event.getCorePointsDelta()))
                        .sum())
                .taskCompletions(safeLongToInt(counts.get(ActivityType.TASK_COMPLETED)))
                .questClaims(safeLongToInt(counts.get(ActivityType.QUEST_CLAIMED)))
                .achievementClaims(safeLongToInt(counts.get(ActivityType.ACHIEVEMENT_CLAIMED)))
                .rewardRedemptions(safeLongToInt(counts.get(ActivityType.REWARD_REDEEMED)))
                .build();
    }

    private ActivityItemResponse toActivityItemResponse(UserActivityEvent event) {
        return ActivityItemResponse.builder()
                .id(event.getId())
                .type(event.getType())
                .title(event.getTitle())
                .description(event.getDescription())
                .xpDelta(event.getXpDelta())
                .corePointsDelta(event.getCorePointsDelta())
                .occurredAt(event.getOccurredAt())
                .metadata(toMetadataMap(event.getMetadataJson()))
                .build();
    }

    private int sumXp(List<UserActivityEvent> events) {
        return events.stream()
                .mapToInt(event -> Math.max(0, event.getXpDelta()))
                .sum();
    }

    private BigDecimal calculateDeltaPercent(int currentTotalXp, int previousTotalXp) {
        if (previousTotalXp == 0) {
            return currentTotalXp == 0 ? BigDecimal.ZERO : null;
        }

        return BigDecimal.valueOf(currentTotalXp - previousTotalXp)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(previousTotalXp), 2, RoundingMode.HALF_UP);
    }

    private String toMetadataJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            log.warn("Could not serialize activity metadata", ex);
            return null;
        }
    }

    private Map<String, Object> toMetadataMap(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException ex) {
            log.warn("Could not deserialize activity metadata for response", ex);
            return new LinkedHashMap<>();
        }
    }

    private int safeLongToInt(Long value) {
        if (value == null) {
            return 0;
        }
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return value.intValue();
    }
}
