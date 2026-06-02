package com.davidrr.grindprotocol.quest.dto;

import com.davidrr.grindprotocol.quest.enums.QuestFrequency;
import com.davidrr.grindprotocol.quest.enums.QuestStatus;
import com.davidrr.grindprotocol.quest.enums.QuestType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestResponse {

    private Long questId;

    private String code;
    private String name;
    private String description;

    private QuestType type;
    private QuestFrequency frequency;

    private Long targetValue;
    private Long progressValue;

    private Long xpReward;
    private Long corePointsReward;

    private QuestStatus status;

    private LocalDate periodStart;
    private LocalDate periodEnd;

    private LocalDateTime completedAt;
    private LocalDateTime claimedAt;
}