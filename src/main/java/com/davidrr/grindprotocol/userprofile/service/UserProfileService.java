package com.davidrr.grindprotocol.userprofile.service;

import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;

public interface UserProfileService {

    UserProfile createDefaultProfile(User user);

    UserProfile getByUserId(Long userId);
}