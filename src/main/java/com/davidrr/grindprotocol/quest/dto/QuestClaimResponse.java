package com.davidrr.grindprotocol.quest.dto;

import com.davidrr.grindprotocol.quest.enums.QuestStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestClaimResponse {

    private Long questId;

    private String code;
    private String name;

    private Long xpReward;
    private Long corePointsReward;

    private QuestStatus status;
}