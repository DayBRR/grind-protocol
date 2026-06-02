package com.davidrr.grindprotocol.quest.service;

import com.davidrr.grindprotocol.quest.enums.QuestFrequency;
import com.davidrr.grindprotocol.quest.enums.QuestStatus;
import com.davidrr.grindprotocol.quest.enums.QuestType;
import com.davidrr.grindprotocol.quest.model.Quest;
import com.davidrr.grindprotocol.quest.model.UserQuest;
import com.davidrr.grindprotocol.quest.repository.QuestRepository;
import com.davidrr.grindprotocol.quest.repository.UserQuestRepository;
import com.davidrr.grindprotocol.quest.service.impl.QuestEvaluationServiceImpl;
import com.davidrr.grindprotocol.task.repository.TaskCompletionRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestEvaluationServiceImplTest {

    @Mock private QuestRepository questRepository;
    @Mock private UserQuestRepository userQuestRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private QuestPeriodResolver questPeriodResolver;
    @Mock private TaskCompletionRepository taskCompletionRepository;

    @InjectMocks
    private QuestEvaluationServiceImpl questEvaluationService;

    @Test
    @DisplayName("evaluateQuests debe crear UserQuest ACTIVE si no alcanza el objetivo")
    void evaluateQuests_shouldCreateActiveUserQuestWhenTargetNotReached() {
        assertEvaluationCreatesQuestWithProgress(2L, QuestStatus.ACTIVE);
    }

    @Test
    @DisplayName("evaluateQuests debe completar UserQuest si alcanza el objetivo")
    void evaluateQuests_shouldCompleteUserQuestWhenTargetReached() {
        assertEvaluationCreatesQuestWithProgress(3L, QuestStatus.COMPLETED);
    }

    @Test
    @DisplayName("evaluateQuests debe actualizar progreso de UserQuest existente")
    void evaluateQuests_shouldUpdateExistingUserQuestProgress() {
        UserProfile userProfile = userProfile(1L);
        Quest quest = quest(1L);
        LocalDate today = LocalDate.now();

        UserQuest existing = userQuest(quest, userProfile, QuestStatus.ACTIVE);
        existing.setProgressValue(1L);

        mockBaseEvaluation(userProfile, quest, today);

        when(userQuestRepository.findByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
                1L, 1L, today, today
        )).thenReturn(Optional.of(existing));
        when(taskCompletionRepository.countByUserIdAndCompletionDateBetween(1L, today, today))
                .thenReturn(2L);

        questEvaluationService.evaluateQuests(1L);

        assertThat(existing.getProgressValue()).isEqualTo(2L);
        assertThat(existing.getStatus()).isEqualTo(QuestStatus.ACTIVE);

        verify(userQuestRepository).save(existing);
    }

    @Test
    @DisplayName("evaluateQuests debe completar UserQuest existente ACTIVE")
    void evaluateQuests_shouldCompleteExistingActiveUserQuest() {
        UserProfile userProfile = userProfile(1L);
        Quest quest = quest(1L);
        LocalDate today = LocalDate.now();

        UserQuest existing = userQuest(quest, userProfile, QuestStatus.ACTIVE);
        existing.setProgressValue(2L);

        mockBaseEvaluation(userProfile, quest, today);

        when(userQuestRepository.findByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
                1L, 1L, today, today
        )).thenReturn(Optional.of(existing));
        when(taskCompletionRepository.countByUserIdAndCompletionDateBetween(1L, today, today))
                .thenReturn(3L);

        questEvaluationService.evaluateQuests(1L);

        assertThat(existing.getProgressValue()).isEqualTo(3L);
        assertThat(existing.getStatus()).isEqualTo(QuestStatus.COMPLETED);
        assertThat(existing.getCompletedAt()).isNotNull();

        verify(userQuestRepository).save(existing);
    }

    @Test
    @DisplayName("evaluateQuests no debe modificar UserQuest CLAIMED")
    void evaluateQuests_shouldNotModifyClaimedUserQuest() {
        UserProfile userProfile = userProfile(1L);
        Quest quest = quest(1L);
        LocalDate today = LocalDate.now();

        UserQuest existing = userQuest(quest, userProfile, QuestStatus.CLAIMED);
        existing.setProgressValue(3L);
        existing.setCompletedAt(LocalDateTime.now().minusHours(2));
        existing.setClaimedAt(LocalDateTime.now().minusHours(1));

        mockBaseEvaluation(userProfile, quest, today);

        when(userQuestRepository.findByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
                1L, 1L, today, today
        )).thenReturn(Optional.of(existing));

        questEvaluationService.evaluateQuests(1L);

        assertThat(existing.getProgressValue()).isEqualTo(3L);
        assertThat(existing.getStatus()).isEqualTo(QuestStatus.CLAIMED);

        verify(userQuestRepository, never()).save(any());
        verifyNoInteractions(taskCompletionRepository);
    }

    private void assertEvaluationCreatesQuestWithProgress(Long progress, QuestStatus expectedStatus) {
        UserProfile userProfile = userProfile(1L);
        Quest quest = quest(1L);
        LocalDate today = LocalDate.now();

        mockBaseEvaluation(userProfile, quest, today);

        when(userQuestRepository.findByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
                1L, 1L, today, today
        )).thenReturn(Optional.empty());
        when(taskCompletionRepository.countByUserIdAndCompletionDateBetween(1L, today, today))
                .thenReturn(progress);

        ArgumentCaptor<UserQuest> captor = ArgumentCaptor.forClass(UserQuest.class);

        questEvaluationService.evaluateQuests(1L);

        verify(userQuestRepository).save(captor.capture());

        UserQuest saved = captor.getValue();

        assertThat(saved.getUserProfile()).isEqualTo(userProfile);
        assertThat(saved.getQuest()).isEqualTo(quest);
        assertThat(saved.getProgressValue()).isEqualTo(progress);
        assertThat(saved.getStatus()).isEqualTo(expectedStatus);
        assertThat(saved.getPeriodStart()).isEqualTo(today);
        assertThat(saved.getPeriodEnd()).isEqualTo(today);

        if (expectedStatus == QuestStatus.COMPLETED) {
            assertThat(saved.getCompletedAt()).isNotNull();
        } else {
            assertThat(saved.getCompletedAt()).isNull();
        }
    }

    private void mockBaseEvaluation(UserProfile userProfile, Quest quest, LocalDate today) {
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(userProfile));
        when(questRepository.findByEnabledTrueOrderByIdAsc()).thenReturn(List.of(quest));
        when(questPeriodResolver.resolvePeriodStart(QuestFrequency.DAILY, today)).thenReturn(today);
        when(questPeriodResolver.resolvePeriodEnd(QuestFrequency.DAILY, today)).thenReturn(today);
    }

    private Quest quest(Long id) {
        Quest quest = new Quest();
        quest.setId(id);
        quest.setCode("DAILY_3_TASKS");
        quest.setName("Daily Grinder");
        quest.setDescription("Complete 3 tasks today.");
        quest.setType(QuestType.TASK_COMPLETION_COUNT);
        quest.setFrequency(QuestFrequency.DAILY);
        quest.setTargetValue(3L);
        quest.setXpReward(50L);
        quest.setCorePointsReward(5L);
        quest.setEnabled(true);
        return quest;
    }

    private UserQuest userQuest(Quest quest, UserProfile userProfile, QuestStatus status) {
        UserQuest userQuest = new UserQuest();
        userQuest.setId(10L);
        userQuest.setQuest(quest);
        userQuest.setUserProfile(userProfile);
        userQuest.setProgressValue(0L);
        userQuest.setStatus(status);
        userQuest.setPeriodStart(LocalDate.now());
        userQuest.setPeriodEnd(LocalDate.now());
        return userQuest;
    }

    private UserProfile userProfile(Long userId) {
        User user = User.builder()
                .id(userId)
                .username("david")
                .email("david@test.com")
                .password("encoded")
                .role("USER")
                .enabled(true)
                .build();

        return UserProfile.builder()
                .id(20L)
                .user(user)
                .displayName("David")
                .dailyTaskGoal(3)
                .totalXp(0L)
                .level(1)
                .corePoints(0L)
                .currentStreak(0)
                .bestStreak(0)
                .build();
    }
}
