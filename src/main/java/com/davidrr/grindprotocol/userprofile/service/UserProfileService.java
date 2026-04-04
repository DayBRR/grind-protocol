package com.davidrr.grindprotocol.userprofile.service;

import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.userprofile.dto.UpdateUserProfileRequest;
import com.davidrr.grindprotocol.userprofile.dto.UserProfileResponse;

public interface UserProfileService {

    void createDefaultProfile(User user);

    UserProfileResponse getCurrentUserProfile(Long currentUserId);

    UserProfileResponse updateProfile(Long currentUserId, UpdateUserProfileRequest request);
}