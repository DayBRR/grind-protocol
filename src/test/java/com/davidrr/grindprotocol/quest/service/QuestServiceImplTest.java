package com.davidrr.grindprotocol.quest.service;

import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.progression.service.ProgressionService;
import com.davidrr.grindprotocol.quest.dto.QuestClaimResponse;
import com.davidrr.grindprotocol.quest.dto.QuestResponse;
import com.davidrr.grindprotocol.quest.enums.QuestFrequency;
import com.davidrr.grindprotocol.quest.enums.QuestStatus;
import com.davidrr.grindprotocol.quest.enums.QuestType;
import com.davidrr.grindprotocol.quest.mapper.QuestMapper;
import com.davidrr.grindprotocol.quest.model.Quest;
import com.davidrr.grindprotocol.quest.model.UserQuest;
import com.davidrr.grindprotocol.quest.repository.QuestRepository;
import com.davidrr.grindprotocol.quest.repository.UserQuestRepository;
import com.davidrr.grindprotocol.quest.service.impl.QuestServiceImpl;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestServiceImplTest {

    @Mock private QuestRepository questRepository;
    @Mock private UserQuestRepository userQuestRepository;
    @Mock private QuestPeriodResolver questPeriodResolver;
    @Mock private QuestEvaluationService questEvaluationService;
    @Mock private QuestMapper questMapper;
    @Mock private ProgressionService progressionService;

    @InjectMocks
    private QuestServiceImpl questService;

    @Test
    @DisplayName("getQuests debe devolver quests mapeadas con el estado del periodo actual")
    void getQuests_shouldReturnMappedQuests() {
        Quest quest = quest(1L);
        UserProfile userProfile = userProfile(1L);
        UserQuest userQuest = userQuest(quest, userProfile, QuestStatus.ACTIVE);
        QuestResponse response = mock(QuestResponse.class);

        LocalDate today = LocalDate.now();

        when(questRepository.findByEnabledTrueOrderByIdAsc()).thenReturn(List.of(quest));
        when(questPeriodResolver.resolvePeriodStart(QuestFrequency.DAILY, today)).thenReturn(today);
        when(questPeriodResolver.resolvePeriodEnd(QuestFrequency.DAILY, today)).thenReturn(today);
        when(userQuestRepository.findByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
                1L, 1L, today, today
        )).thenReturn(Optional.of(userQuest));
        when(questMapper.toResponse(quest, userQuest)).thenReturn(response);

        List<QuestResponse> result = questService.getQuests(1L);

        assertThat(result).containsExactly(response);
        verify(questMapper).toResponse(quest, userQuest);
    }

    @Test
    @DisplayName("claimQuest debe reclamar una quest completada y aplicar recompensas")
    void claimQuest_shouldClaimCompletedQuest() {
        Quest quest = quest(1L);
        UserProfile userProfile = userProfile(1L);
        UserQuest userQuest = userQuest(quest, userProfile, QuestStatus.COMPLETED);
        QuestClaimResponse response = mock(QuestClaimResponse.class);

        LocalDate today = LocalDate.now();

        when(questRepository.findById(1L)).thenReturn(Optional.of(quest));
        when(questPeriodResolver.resolvePeriodStart(QuestFrequency.DAILY, today)).thenReturn(today);
        when(questPeriodResolver.resolvePeriodEnd(QuestFrequency.DAILY, today)).thenReturn(today);
        when(userQuestRepository.findByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
                1L, 1L, today, today
        )).thenReturn(Optional.of(userQuest));
        when(questMapper.toClaimResponse(userQuest)).thenReturn(response);

        QuestClaimResponse result = questService.claimQuest(1L, 1L);

        assertThat(result).isSameAs(response);
        assertThat(userQuest.getStatus()).isEqualTo(QuestStatus.CLAIMED);
        assertThat(userQuest.getClaimedAt()).isNotNull();

        verify(progressionService).addProgressionRewards(userProfile, 50L, 5L);
        verify(userQuestRepository).save(userQuest);
        verify(questMapper).toClaimResponse(userQuest);
    }

    @Test
    @DisplayName("claimQuest debe fallar si la quest no existe")
    void claimQuest_shouldThrowWhenQuestNotFound() {
        when(questRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questService.claimQuest(1L, 1L))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(userQuestRepository);
        verifyNoInteractions(progressionService);
        verifyNoInteractions(questMapper);
    }

    @Test
    @DisplayName("claimQuest debe fallar si no existe UserQuest para el periodo actual")
    void claimQuest_shouldThrowWhenUserQuestNotFoundForCurrentPeriod() {
        Quest quest = quest(1L);
        LocalDate today = LocalDate.now();

        when(questRepository.findById(1L)).thenReturn(Optional.of(quest));
        when(questPeriodResolver.resolvePeriodStart(QuestFrequency.DAILY, today)).thenReturn(today);
        when(questPeriodResolver.resolvePeriodEnd(QuestFrequency.DAILY, today)).thenReturn(today);
        when(userQuestRepository.findByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
                1L, 1L, today, today
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questService.claimQuest(1L, 1L))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(progressionService);
        verifyNoInteractions(questMapper);
    }

    @Test
    @DisplayName("claimQuest debe fallar si la quest no está completada")
    void claimQuest_shouldThrowWhenQuestNotCompleted() {
        Quest quest = quest(1L);
        UserQuest userQuest = userQuest(quest, userProfile(1L), QuestStatus.ACTIVE);
        LocalDate today = LocalDate.now();

        when(questRepository.findById(1L)).thenReturn(Optional.of(quest));
        when(questPeriodResolver.resolvePeriodStart(QuestFrequency.DAILY, today)).thenReturn(today);
        when(questPeriodResolver.resolvePeriodEnd(QuestFrequency.DAILY, today)).thenReturn(today);
        when(userQuestRepository.findByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
                1L, 1L, today, today
        )).thenReturn(Optional.of(userQuest));

        assertThatThrownBy(() -> questService.claimQuest(1L, 1L))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(progressionService);
        verify(userQuestRepository, never()).save(any());
        verifyNoInteractions(questMapper);
    }

    @Test
    @DisplayName("claimQuest debe fallar si la quest ya fue reclamada")
    void claimQuest_shouldThrowWhenQuestAlreadyClaimed() {
        Quest quest = quest(1L);
        UserQuest userQuest = userQuest(quest, userProfile(1L), QuestStatus.CLAIMED);
        LocalDate today = LocalDate.now();

        when(questRepository.findById(1L)).thenReturn(Optional.of(quest));
        when(questPeriodResolver.resolvePeriodStart(QuestFrequency.DAILY, today)).thenReturn(today);
        when(questPeriodResolver.resolvePeriodEnd(QuestFrequency.DAILY, today)).thenReturn(today);
        when(userQuestRepository.findByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
                1L, 1L, today, today
        )).thenReturn(Optional.of(userQuest));

        assertThatThrownBy(() -> questService.claimQuest(1L, 1L))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(progressionService);
        verify(userQuestRepository, never()).save(any());
        verifyNoInteractions(questMapper);
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
        userQuest.setProgressValue(3L);
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
