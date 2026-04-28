package com.davidrr.grindprotocol.progression.service.impl;

import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.task.model.DailyProgress;
import com.davidrr.grindprotocol.task.repository.DailyProgressRepository;
import com.davidrr.grindprotocol.progression.service.StreakService;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class StreakServiceImpl implements StreakService {

    private final UserProfileRepository userProfileRepository;
    private final DailyProgressRepository dailyProgressRepository;

    @Override
    public void finalizeDay(Long userId, LocalDate date) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.UserProfile.NOT_FOUND,
                        "User profile not found for user id: " + userId
                ));

        DailyProgress progress = dailyProgressRepository.findByUserIdAndProgressDate(userId, date)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.DailyProgress.NOT_FOUND,
                        "Daily progress not found for user id: " + userId + " and date: " + date
                ));

        if (profile.getLastEvaluatedDate() != null && profile.getLastEvaluatedDate().isEqual(date)) {
            return;
        }

        if (progress.isDayQualified()) {
            if (profile.getLastEvaluatedDate() != null && profile.getLastEvaluatedDate().plusDays(1).isEqual(date)) {
                profile.setCurrentStreak(profile.getCurrentStreak() + 1);
            } else {
                profile.setCurrentStreak(1);
            }

            if (profile.getCurrentStreak() > profile.getBestStreak()) {
                profile.setBestStreak(profile.getCurrentStreak());
            }
        } else {
            profile.setCurrentStreak(0);
        }

        profile.setLastEvaluatedDate(date);
        userProfileRepository.save(profile);
    }
}