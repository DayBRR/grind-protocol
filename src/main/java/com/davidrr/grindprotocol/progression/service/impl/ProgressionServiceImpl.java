package com.davidrr.grindprotocol.progression.service.impl;

import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.task.model.TaskCompletion;
import com.davidrr.grindprotocol.progression.service.ProgressionService;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProgressionServiceImpl implements ProgressionService {

    private final UserProfileRepository userProfileRepository;

    @Override
    public void applyTaskCompletionProgress(Long userId, TaskCompletion completion) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.UserProfile.NOT_FOUND,
                        "User profile not found for user id: " + userId
                ));

        profile.setTotalXp(profile.getTotalXp() + completion.getAwardedXp());
        profile.setCorePoints(profile.getCorePoints() + completion.getAwardedCorePoints());

        userProfileRepository.save(profile);
    }
}