package com.davidrr.grindprotocol.quest.mapper;

import com.davidrr.grindprotocol.quest.dto.QuestClaimResponse;
import com.davidrr.grindprotocol.quest.dto.QuestResponse;
import com.davidrr.grindprotocol.quest.enums.QuestStatus;
import com.davidrr.grindprotocol.quest.model.Quest;
import com.davidrr.grindprotocol.quest.model.UserQuest;
import org.springframework.stereotype.Component;

@Component
public class QuestMapper {

    public QuestResponse toResponse(
            Quest quest,
            UserQuest userQuest
    ) {
        return QuestResponse.builder()
                .questId(quest.getId())
                .code(quest.getCode())
                .name(quest.getName())
                .description(quest.getDescription())
                .type(quest.getType())
                .frequency(quest.getFrequency())
                .targetValue(quest.getTargetValue())
                .progressValue(userQuest != null ? userQuest.getProgressValue() : 0L)
                .xpReward(quest.getXpReward())
                .corePointsReward(quest.getCorePointsReward())
                .status(userQuest != null ? userQuest.getStatus() : QuestStatus.ACTIVE)
                .periodStart(userQuest != null ? userQuest.getPeriodStart() : null)
                .periodEnd(userQuest != null ? userQuest.getPeriodEnd() : null)
                .completedAt(userQuest != null ? userQuest.getCompletedAt() : null)
                .claimedAt(userQuest != null ? userQuest.getClaimedAt() : null)
                .build();
    }

    public QuestClaimResponse toClaimResponse(UserQuest userQuest) {
        Quest quest = userQuest.getQuest();

        return QuestClaimResponse.builder()
                .questId(quest.getId())
                .code(quest.getCode())
                .name(quest.getName())
                .xpReward(quest.getXpReward())
                .corePointsReward(quest.getCorePointsReward())
                .status(userQuest.getStatus())
                .build();
    }
}