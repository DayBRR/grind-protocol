package com.davidrr.grindprotocol.activity.controller;

import com.davidrr.grindprotocol.activity.dto.RecentActivityResponse;
import com.davidrr.grindprotocol.activity.service.UserActivityEventService;
import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import com.davidrr.grindprotocol.utils.TestAuthenticatedUserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityControllerTest {

    @Mock
    private UserActivityEventService userActivityEventService;

    @InjectMocks
    private ActivityController activityController;

    @Test
    @DisplayName("getRecentActivity debe delegar en UserActivityEventService con el usuario autenticado")
    void getRecentActivity_shouldDelegateToUserActivityEventService() {
        AuthenticatedUser currentUser = TestAuthenticatedUserFactory.defaultUser();
        RecentActivityResponse response = RecentActivityResponse.builder()
                .items(List.of())
                .build();

        when(userActivityEventService.getRecentActivity(1L, 10)).thenReturn(response);

        RecentActivityResponse result = activityController.getRecentActivity(currentUser, 10);

        assertThat(result).isSameAs(response);
        verify(userActivityEventService).getRecentActivity(1L, 10);
    }
}
