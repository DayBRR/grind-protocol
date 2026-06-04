package com.davidrr.grindprotocol.activity.service;

import com.davidrr.grindprotocol.activity.dto.RecentActivityResponse;
import com.davidrr.grindprotocol.activity.dto.WeeklyProgressionSummaryResponse;
import com.davidrr.grindprotocol.achievement.model.UserAchievement;
import com.davidrr.grindprotocol.quest.model.UserQuest;
import com.davidrr.grindprotocol.reward.model.RewardRedemption;
import com.davidrr.grindprotocol.task.model.TaskCompletion;

import java.time.LocalDate;

public interface UserActivityEventService {

    void recordTaskCompleted(TaskCompletion completion);

    void recordQuestClaimed(UserQuest userQuest);

    void recordAchievementClaimed(UserAchievement userAchievement);

    void recordRewardRedeemed(RewardRedemption redemption);

    void recordRewardUsed(RewardRedemption redemption);

    RecentActivityResponse getRecentActivity(Long userId, int limit);

    WeeklyProgressionSummaryResponse getWeeklySummary(Long userId, LocalDate referenceDate);
}
