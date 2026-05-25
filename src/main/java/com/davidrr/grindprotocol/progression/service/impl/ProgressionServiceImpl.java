package com.davidrr.grindprotocol.progression.service.impl;

import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.progression.service.ProgressionService;
import com.davidrr.grindprotocol.progression.service.calculator.LevelCalculator;
import com.davidrr.grindprotocol.task.model.TaskCompletion;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProgressionServiceImpl implements ProgressionService {

    private final UserProfileRepository userProfileRepository;
    private final LevelCalculator levelCalculator;

    @Override
    public void applyTaskCompletionProgress(Long userId, TaskCompletion completion) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.UserProfile.NOT_FOUND,
                        "User profile not found for user id: " + userId
                ));

        long previousXp = profile.getTotalXp();
        int previousLevel = profile.getLevel();

        long newTotalXp = previousXp + completion.getAwardedXp();
        int newLevel = levelCalculator.calculateLevel(newTotalXp);

        profile.setTotalXp(newTotalXp);
        profile.setLevel(newLevel);
        profile.setCorePoints(profile.getCorePoints() + completion.getAwardedCorePoints());

        if (newLevel > previousLevel) {
            log.info(
                    "LEVEL UP userId={} level {} -> {} totalXp={}",
                    userId,
                    previousLevel,
                    newLevel,
                    newTotalXp
            );
        }

        userProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public void addProgressionRewards(UserProfile userProfile, Long xp, Long corePoints) {

        userProfile.setTotalXp(
                userProfile.getTotalXp() + xp
        );

        userProfile.setCorePoints(
                userProfile.getCorePoints() + corePoints
        );

        userProfile.setLevel(
                levelCalculator.calculateLevel(
                        userProfile.getTotalXp()
                )
        );

        userProfileRepository.save(userProfile);
    }
}