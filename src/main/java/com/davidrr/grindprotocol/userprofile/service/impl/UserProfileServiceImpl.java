package com.davidrr.grindprotocol.userprofile.service.impl;

import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.common.exception.ResourceNotFoundException;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.userprofile.dto.UpdateUserProfileRequest;
import com.davidrr.grindprotocol.userprofile.dto.UserProfileResponse;
import com.davidrr.grindprotocol.userprofile.mapper.UserProfileMapper;
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
    private final UserProfileMapper userProfileMapper;

    @Override
    @Transactional
    public void createDefaultProfile(User user) {
        boolean exists = userProfileRepository.existsByUserId(user.getId());
        if (exists) {
            return;
        }

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

        userProfileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(Long currentUserId) {
        UserProfile profile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.UserProfile.NOT_FOUND,
                        "User profile not found for user id: " + currentUserId
                ));

        return userProfileMapper.toResponse(profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(Long currentUserId, UpdateUserProfileRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.UserProfile.NOT_FOUND,
                        "User profile not found for user id: " + currentUserId
                ));

        userProfileMapper.updateEntityFromRequest(request, profile);

        UserProfile saved = userProfileRepository.save(profile);

        return userProfileMapper.toResponse(saved);
    }
}