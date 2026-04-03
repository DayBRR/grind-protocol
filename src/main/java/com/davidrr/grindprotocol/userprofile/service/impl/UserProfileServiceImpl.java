package com.davidrr.grindprotocol.userprofile.service.impl;

import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import com.davidrr.grindprotocol.userprofile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public UserProfile createDefaultProfile(User user) {
        return userProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserProfile profile = UserProfile.builder()
                            .user(user)
                            .displayName(user.getUsername())
                            .dailyTaskGoal(3)
                            .totalXp(0L)
                            .corePoints(0L)
                            .currentStreak(0)
                            .bestStreak(0)
                            .lastEvaluatedDate(null)
                            .build();

                    return userProfileRepository.save(profile);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfile getByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "User profile not found for user id: " + userId
                ));
    }
}