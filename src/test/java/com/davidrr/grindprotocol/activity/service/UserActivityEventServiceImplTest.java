package com.davidrr.grindprotocol.activity.service;

import com.davidrr.grindprotocol.activity.dto.ActivityItemResponse;
import com.davidrr.grindprotocol.activity.dto.RecentActivityResponse;
import com.davidrr.grindprotocol.activity.dto.WeeklyProgressionSummaryResponse;
import com.davidrr.grindprotocol.activity.enums.ActivitySourceType;
import com.davidrr.grindprotocol.activity.enums.ActivityType;
import com.davidrr.grindprotocol.activity.model.UserActivityEvent;
import com.davidrr.grindprotocol.activity.repository.UserActivityEventRepository;
import com.davidrr.grindprotocol.activity.service.impl.UserActivityEventServiceImpl;
import com.davidrr.grindprotocol.reward.enums.RewardCategory;
import com.davidrr.grindprotocol.reward.enums.RewardRedemptionStatus;
import com.davidrr.grindprotocol.reward.enums.RewardType;
import com.davidrr.grindprotocol.reward.model.Reward;
import com.davidrr.grindprotocol.reward.model.RewardRedemption;
import com.davidrr.grindprotocol.task.model.Task;
import com.davidrr.grindprotocol.task.model.TaskCompletion;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import com.davidrr.grindprotocol.utils.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserActivityEventServiceImplTest {

    @Mock
    private UserActivityEventRepository userActivityEventRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    private UserActivityEventServiceImpl userActivityEventService;

    private User user;
    private UserProfile userProfile;

    @BeforeEach
    void setUp() {
        userActivityEventService = new UserActivityEventServiceImpl(
                userActivityEventRepository,
                userProfileRepository,
                new ObjectMapper()
        );

        user = TestDataFactory.user();
        userProfile = TestDataFactory.userProfile(user);
    }

    @Test
    @DisplayName("recordTaskCompleted debe crear un evento TASK_COMPLETED con XP, Core Points y metadata")
    void recordTaskCompleted_shouldCreateTaskCompletedEvent() {
        Task task = TestDataFactory.task(10L, user, true);
        task.setBaseXp(40);
        TaskCompletion completion = TestDataFactory.taskCompletion(100L, task, user);
        completion.setAwardedXp(40);
        completion.setAwardedCorePoints(4);
        completion.setNotes("Done");

        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(userProfile));

        ArgumentCaptor<UserActivityEvent> eventCaptor = ArgumentCaptor.forClass(UserActivityEvent.class);
        when(userActivityEventRepository.save(eventCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userActivityEventService.recordTaskCompleted(completion);

        UserActivityEvent saved = eventCaptor.getValue();
        assertThat(saved.getUserProfile()).isEqualTo(userProfile);
        assertThat(saved.getType()).isEqualTo(ActivityType.TASK_COMPLETED);
        assertThat(saved.getSourceType()).isEqualTo(ActivitySourceType.TASK_COMPLETION);
        assertThat(saved.getSourceId()).isEqualTo(100L);
        assertThat(saved.getTitle()).isEqualTo("Completed task: Estudiar Spring");
        assertThat(saved.getDescription()).isEqualTo("Done");
        assertThat(saved.getXpDelta()).isEqualTo(40);
        assertThat(saved.getCorePointsDelta()).isEqualTo(4);
        assertThat(saved.getOccurredAt()).isEqualTo(completion.getCompletedAt());
        assertThat(saved.getMetadataJson()).contains("taskId", "category", "difficulty");
    }

    @Test
    @DisplayName("recordRewardRedeemed debe crear un evento con Core Points negativo")
    void recordRewardRedeemed_shouldCreateNegativeCorePointsEvent() {
        Reward reward = new Reward();
        reward.setId(10L);
        reward.setName("Coffee");
        reward.setDescription("Take a coffee");
        reward.setType(RewardType.REAL);
        reward.setCategory(RewardCategory.FOOD);
        reward.setCostCorePoints(25L);

        RewardRedemption redemption = new RewardRedemption();
        redemption.setId(50L);
        redemption.setReward(reward);
        redemption.setUserProfile(userProfile);
        redemption.setStatus(RewardRedemptionStatus.REDEEMED);
        redemption.setCostPaid(25L);
        redemption.setRedeemedAt(LocalDateTime.of(2026, 6, 3, 12, 0));

        ArgumentCaptor<UserActivityEvent> eventCaptor = ArgumentCaptor.forClass(UserActivityEvent.class);
        when(userActivityEventRepository.save(eventCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userActivityEventService.recordRewardRedeemed(redemption);

        UserActivityEvent saved = eventCaptor.getValue();
        assertThat(saved.getUserProfile()).isEqualTo(userProfile);
        assertThat(saved.getType()).isEqualTo(ActivityType.REWARD_REDEEMED);
        assertThat(saved.getSourceType()).isEqualTo(ActivitySourceType.REWARD_REDEMPTION);
        assertThat(saved.getSourceId()).isEqualTo(50L);
        assertThat(saved.getTitle()).isEqualTo("Redeemed reward: Coffee");
        assertThat(saved.getXpDelta()).isZero();
        assertThat(saved.getCorePointsDelta()).isEqualTo(-25);
        assertThat(saved.getOccurredAt()).isEqualTo(redemption.getRedeemedAt());
    }

    @Test
    @DisplayName("getRecentActivity debe limitar resultados y mapear metadata JSON")
    void getRecentActivity_shouldLimitAndMapMetadata() {
        UserActivityEvent event = activityEvent(
                1L,
                ActivityType.TASK_COMPLETED,
                ActivitySourceType.TASK_COMPLETION,
                100L,
                "Completed task: Workout",
                "Great job",
                20,
                2,
                LocalDateTime.of(2026, 6, 4, 10, 30)
        );
        event.setMetadataJson("{\"taskId\":10,\"category\":\"BODY\"}");

        when(userActivityEventRepository.findByUserProfileUserIdOrderByOccurredAtDesc(
                eq(1L),
                any(Pageable.class)
        )).thenReturn(List.of(event));

        RecentActivityResponse response = userActivityEventService.getRecentActivity(1L, 200);

        assertThat(response.items()).hasSize(1);
        ActivityItemResponse item = response.items().get(0);
        assertThat(item.id()).isEqualTo(1L);
        assertThat(item.type()).isEqualTo(ActivityType.TASK_COMPLETED);
        assertThat(item.title()).isEqualTo("Completed task: Workout");
        assertThat(item.xpDelta()).isEqualTo(20);
        assertThat(item.corePointsDelta()).isEqualTo(2);
        assertThat(item.metadata()).containsEntry("category", "BODY");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userActivityEventRepository).findByUserProfileUserIdOrderByOccurredAtDesc(eq(1L), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(50);
    }

    @Test
    @DisplayName("getWeeklySummary debe agrupar eventos de lunes a domingo y comparar con la semana anterior")
    void getWeeklySummary_shouldGroupEventsByWeek() {
        LocalDate referenceDate = LocalDate.of(2026, 6, 4); // Thursday
        LocalDateTime monday = LocalDateTime.of(2026, 6, 1, 9, 0);
        LocalDateTime tuesday = LocalDateTime.of(2026, 6, 2, 18, 0);

        UserActivityEvent taskEvent = activityEvent(
                1L,
                ActivityType.TASK_COMPLETED,
                ActivitySourceType.TASK_COMPLETION,
                100L,
                "Completed task",
                null,
                20,
                2,
                monday
        );
        UserActivityEvent questEvent = activityEvent(
                2L,
                ActivityType.QUEST_CLAIMED,
                ActivitySourceType.QUEST,
                200L,
                "Claimed quest",
                null,
                50,
                5,
                tuesday
        );
        UserActivityEvent rewardEvent = activityEvent(
                3L,
                ActivityType.REWARD_REDEEMED,
                ActivitySourceType.REWARD_REDEMPTION,
                300L,
                "Redeemed reward",
                null,
                0,
                -10,
                tuesday.plusHours(1)
        );
        UserActivityEvent previousWeekEvent = activityEvent(
                4L,
                ActivityType.TASK_COMPLETED,
                ActivitySourceType.TASK_COMPLETION,
                400L,
                "Previous task",
                null,
                35,
                3,
                LocalDateTime.of(2026, 5, 26, 10, 0)
        );

        when(userActivityEventRepository.findByUserProfileUserIdAndOccurredAtBetweenOrderByOccurredAtAsc(
                eq(1L),
                eq(LocalDate.of(2026, 6, 1).atStartOfDay()),
                any(LocalDateTime.class)
        )).thenReturn(List.of(taskEvent, questEvent, rewardEvent));

        when(userActivityEventRepository.findByUserProfileUserIdAndOccurredAtBetweenOrderByOccurredAtAsc(
                eq(1L),
                eq(LocalDate.of(2026, 5, 25).atStartOfDay()),
                any(LocalDateTime.class)
        )).thenReturn(List.of(previousWeekEvent));

        WeeklyProgressionSummaryResponse response = userActivityEventService.getWeeklySummary(1L, referenceDate);

        assertThat(response.weekStart()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(response.weekEnd()).isEqualTo(LocalDate.of(2026, 6, 7));
        assertThat(response.totalXp()).isEqualTo(70);
        assertThat(response.previousWeekTotalXp()).isEqualTo(35);
        assertThat(response.deltaPercent()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2));
        assertThat(response.days()).hasSize(7);
        assertThat(response.days().get(0).xpEarned()).isEqualTo(20);
        assertThat(response.days().get(0).taskCompletions()).isEqualTo(1);
        assertThat(response.days().get(1).xpEarned()).isEqualTo(50);
        assertThat(response.days().get(1).questClaims()).isEqualTo(1);
        assertThat(response.days().get(1).rewardRedemptions()).isEqualTo(1);
        assertThat(response.days().get(1).corePointsEarned()).isEqualTo(5);
    }

    private UserActivityEvent activityEvent(
            Long id,
            ActivityType type,
            ActivitySourceType sourceType,
            Long sourceId,
            String title,
            String description,
            Integer xpDelta,
            Integer corePointsDelta,
            LocalDateTime occurredAt
    ) {
        return UserActivityEvent.builder()
                .id(id)
                .userProfile(userProfile)
                .type(type)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .title(title)
                .description(description)
                .xpDelta(xpDelta)
                .corePointsDelta(corePointsDelta)
                .occurredAt(occurredAt)
                .build();
    }
}
