package com.davidrr.grindprotocol.progression.service.impl;

import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.progression.dto.ProgressionSummaryResponse;
import com.davidrr.grindprotocol.progression.service.ProgressionSummaryService;
import com.davidrr.grindprotocol.progression.service.calculator.LevelCalculator;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgressionSummaryServiceImpl implements ProgressionSummaryService {

    private final UserProfileRepository userProfileRepository;
    private final LevelCalculator levelCalculator;

    @Override
    public ProgressionSummaryResponse getSummary(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.UserProfile.NOT_FOUND,
                        "User profile not found for user id: " + userId
                ));

        Long totalXp = profile.getTotalXp();

        return ProgressionSummaryResponse.builder()
                .totalXp(totalXp)
                .level(profile.getLevel())
                .xpForCurrentLevel(levelCalculator.getXpForCurrentLevel(totalXp))
                .xpForNextLevel(levelCalculator.getXpForNextLevel(totalXp))
                .xpProgressInCurrentLevel(levelCalculator.getXpProgressInCurrentLevel(totalXp))
                .xpRemainingForNextLevel(levelCalculator.getXpRemainingForNextLevel(totalXp))
                .corePoints(profile.getCorePoints())
                .currentStreak(profile.getCurrentStreak())
                .bestStreak(profile.getBestStreak())
                .build();
    }
}