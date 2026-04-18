package com.davidrr.grindprotocol.userprofile.controller;

import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import com.davidrr.grindprotocol.userprofile.dto.UpdateUserProfileRequest;
import com.davidrr.grindprotocol.userprofile.dto.UserProfileResponse;
import com.davidrr.grindprotocol.userprofile.service.UserProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.davidrr.grindprotocol.utils.TestAuthenticatedUserFactory.defaultUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserProfileControllerTest {

    @Test
    @DisplayName("getMyProfile debe delegar en el service")
    void getMyProfile_shouldDelegateToService() {
        UserProfileService userProfileService = mock(UserProfileService.class);
        UserProfileController controller = new UserProfileController(userProfileService);

        AuthenticatedUser currentUser = defaultUser();
        UserProfileResponse response = mock(UserProfileResponse.class);

        when(userProfileService.getCurrentUserProfile(1L)).thenReturn(response);

        UserProfileResponse result = controller.getMyProfile(currentUser);

        assertThat(result).isSameAs(response);
        verify(userProfileService).getCurrentUserProfile(1L);
    }

    @Test
    @DisplayName("updateMyProfile debe delegar en el service")
    void updateMyProfile_shouldDelegateToService() {
        UserProfileService userProfileService = mock(UserProfileService.class);
        UserProfileController controller = new UserProfileController(userProfileService);

        AuthenticatedUser currentUser = defaultUser();
        UpdateUserProfileRequest request = mock(UpdateUserProfileRequest.class);
        UserProfileResponse response = mock(UserProfileResponse.class);

        when(userProfileService.updateProfile(1L, request)).thenReturn(response);

        UserProfileResponse result = controller.updateMyProfile(currentUser, request);

        assertThat(result).isSameAs(response);
        verify(userProfileService).updateProfile(1L, request);
    }
}
