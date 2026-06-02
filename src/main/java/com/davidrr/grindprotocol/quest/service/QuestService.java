package com.davidrr.grindprotocol.quest.service;

import com.davidrr.grindprotocol.quest.dto.QuestClaimResponse;
import com.davidrr.grindprotocol.quest.dto.QuestResponse;

import java.util.List;

public interface QuestService {

    List<QuestResponse> getQuests(Long userId);

    void evaluateQuests(Long userId);

    QuestClaimResponse claimQuest(Long questId, Long userId);

}