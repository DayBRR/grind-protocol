package com.davidrr.grindprotocol.quest.service.impl;

import com.davidrr.grindprotocol.quest.enums.QuestStatus;
import com.davidrr.grindprotocol.quest.enums.QuestType;
import com.davidrr.grindprotocol.quest.model.Quest;
import com.davidrr.grindprotocol.quest.model.UserQuest;
import com.davidrr.grindprotocol.quest.repository.QuestRepository;
import com.davidrr.grindprotocol.quest.repository.UserQuestRepository;
import com.davidrr.grindprotocol.quest.service.QuestEvaluationService;
import com.davidrr.grindprotocol.quest.service.QuestPeriodResolver;
import com.davidrr.grindprotocol.task.repository.TaskCompletionRepository;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestEvaluationServiceImpl implements QuestEvaluationService {

    private final QuestRepository questRepository;
    private final UserQuestRepository userQuestRepository;
    private final UserProfileRepository userProfileRepository;
    private final QuestPeriodResolver questPeriodResolver;
    private final TaskCompletionRepository taskCompletionRepository;

    @Override
    @Transactional
    public void evaluateQuests(Long userId) {

        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow();

        LocalDate today = LocalDate.now();

        List<Quest> quests = questRepository.findByEnabledTrueOrderByIdAsc();

        for (Quest quest : quests) {
            evaluateQuest(userProfile, quest, today);
        }
    }

    private void evaluateQuest(
            UserProfile userProfile,
            Quest quest,
            LocalDate referenceDate
    ) {

        LocalDate periodStart = questPeriodResolver.resolvePeriodStart(
                quest.getFrequency(),
                referenceDate
        );

        LocalDate periodEnd = questPeriodResolver.resolvePeriodEnd(
                quest.getFrequency(),
                referenceDate
        );

        UserQuest userQuest = userQuestRepository
                .findByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
                        userProfile.getUser().getId(),
                        quest.getId(),
                        periodStart,
                        periodEnd
                )
                .orElseGet(() -> createUserQuest(
                        userProfile,
                        quest,
                        periodStart,
                        periodEnd
                ));

        if (userQuest.getStatus() == QuestStatus.CLAIMED
                || userQuest.getStatus() == QuestStatus.EXPIRED) {
            return;
        }

        Long progressValue = calculateProgressValue(
                userProfile,
                quest.getType(),
                periodStart,
                periodEnd
        );

        userQuest.setProgressValue(progressValue);

        if (userQuest.getStatus() == QuestStatus.ACTIVE
                && progressValue >= quest.getTargetValue()) {

            userQuest.setStatus(QuestStatus.COMPLETED);
            userQuest.setCompletedAt(LocalDateTime.now());
        }

        userQuestRepository.save(userQuest);
    }

    private UserQuest createUserQuest(
            UserProfile userProfile,
            Quest quest,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {

        UserQuest userQuest = new UserQuest();

        userQuest.setUserProfile(userProfile);
        userQuest.setQuest(quest);
        userQuest.setProgressValue(0L);
        userQuest.setStatus(QuestStatus.ACTIVE);
        userQuest.setPeriodStart(periodStart);
        userQuest.setPeriodEnd(periodEnd);

        return userQuest;
    }

    private Long calculateProgressValue(
            UserProfile userProfile,
            QuestType type,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {

        return switch (type) {
            case TASK_COMPLETION_COUNT -> taskCompletionRepository
                    .countByUserIdAndCompletionDateBetween(
                            userProfile.getUser().getId(),
                            periodStart,
                            periodEnd
                    );

            case XP_GAINED,
                 CORE_POINTS_EARNED,
                 REWARD_REDEMPTION_COUNT -> 0L;
        };
    }
}