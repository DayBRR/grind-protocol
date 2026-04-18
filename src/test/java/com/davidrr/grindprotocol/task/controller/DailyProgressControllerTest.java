package com.davidrr.grindprotocol.task.controller;

import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import com.davidrr.grindprotocol.task.dto.DailyProgressResponse;
import com.davidrr.grindprotocol.task.service.DailyProgressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.davidrr.grindprotocol.utils.TestAuthenticatedUserFactory.defaultUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DailyProgressControllerTest {

    @Test
    @DisplayName("getTodayProgress debe delegar en el service con el id del usuario autenticado")
    void getTodayProgress_shouldDelegateToService() {
        DailyProgressService dailyProgressService = mock(DailyProgressService.class);
        DailyProgressController controller = new DailyProgressController(dailyProgressService);

        AuthenticatedUser currentUser = defaultUser();
        DailyProgressResponse response = mock(DailyProgressResponse.class);

        when(dailyProgressService.getTodayProgress(1L)).thenReturn(response);

        DailyProgressResponse result = controller.getTodayProgress(currentUser);

        assertThat(result).isSameAs(response);
        verify(dailyProgressService).getTodayProgress(1L);
    }
}
