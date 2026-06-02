package com.davidrr.grindprotocol.quest.service.impl;

import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.common.exception.ErrorMessages;
import com.davidrr.grindprotocol.progression.service.ProgressionService;
import com.davidrr.grindprotocol.quest.dto.QuestClaimResponse;
import com.davidrr.grindprotocol.quest.dto.QuestResponse;
import com.davidrr.grindprotocol.quest.enums.QuestStatus;
import com.davidrr.grindprotocol.quest.mapper.QuestMapper;
import com.davidrr.grindprotocol.quest.model.Quest;
import com.davidrr.grindprotocol.quest.model.UserQuest;
import com.davidrr.grindprotocol.quest.repository.QuestRepository;
import com.davidrr.grindprotocol.quest.repository.UserQuestRepository;
import com.davidrr.grindprotocol.quest.service.QuestPeriodResolver;
import com.davidrr.grindprotocol.quest.service.QuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestServiceImpl implements QuestService {

    private final QuestRepository questRepository;
    private final UserQuestRepository userQuestRepository;
    private final QuestPeriodResolver questPeriodResolver;
    private final QuestMapper questMapper;
    private final ProgressionService progressionService;

    @Override
    public List<QuestResponse> getQuests(Long userId) {

        LocalDate today = LocalDate.now();

        List<Quest> quests = questRepository.findByEnabledTrueOrderByIdAsc();

        return quests.stream()
                .map(quest -> {
                    LocalDate periodStart = questPeriodResolver.resolvePeriodStart(
                            quest.getFrequency(),
                            today
                    );

                    LocalDate periodEnd = questPeriodResolver.resolvePeriodEnd(
                            quest.getFrequency(),
                            today
                    );

                    UserQuest userQuest = userQuestRepository
                            .findByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
                                    userId,
                                    quest.getId(),
                                    periodStart,
                                    periodEnd
                            )
                            .orElse(null);

                    return questMapper.toResponse(quest, userQuest);
                })
                .toList();
    }

    @Override
    @Transactional
    public QuestClaimResponse claimQuest(
            Long questId,
            Long userId
    ) {

        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.Quest.NOT_FOUND, ErrorMessages.Quest.NOT_FOUND
                ));

        LocalDate today = LocalDate.now();

        LocalDate periodStart = questPeriodResolver.resolvePeriodStart(
                quest.getFrequency(),
                today
        );

        LocalDate periodEnd = questPeriodResolver.resolvePeriodEnd(
                quest.getFrequency(),
                today
        );

        UserQuest userQuest = userQuestRepository
                .findByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
                        userId,
                        questId,
                        periodStart,
                        periodEnd
                )
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.Quest.NOT_FOUND, ErrorMessages.Quest.NOT_FOUND
                ));

        if (userQuest.getStatus() == QuestStatus.CLAIMED) {
            throw new BusinessException(
                    ErrorCodes.Quest.ALREADY_CLAIMED, ErrorMessages.Quest.ALREADY_CLAIMED
            );
        }

        if (userQuest.getStatus() != QuestStatus.COMPLETED) {
            throw new BusinessException(
                    ErrorCodes.Quest.NOT_COMPLETED, ErrorMessages.Quest.NOT_COMPLETED
            );
        }

        progressionService.addProgressionRewards(
                userQuest.getUserProfile(),
                quest.getXpReward(),
                quest.getCorePointsReward()
        );

        userQuest.setStatus(QuestStatus.CLAIMED);
        userQuest.setClaimedAt(LocalDateTime.now());

        userQuestRepository.save(userQuest);

        return questMapper.toClaimResponse(userQuest);
    }
}