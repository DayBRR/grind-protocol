package com.davidrr.grindprotocol.quest.controller;

import com.davidrr.grindprotocol.quest.dto.QuestClaimResponse;
import com.davidrr.grindprotocol.quest.dto.QuestResponse;
import com.davidrr.grindprotocol.quest.service.QuestService;
import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.davidrr.grindprotocol.utils.TestAuthenticatedUserFactory.defaultUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class QuestControllerTest {

    @Test
    @DisplayName("getQuests debe delegar en el service usando el usuario autenticado")
    void getQuests_shouldDelegateToService() {
        QuestService questService = mock(QuestService.class);
        QuestController controller = new QuestController(questService);

        AuthenticatedUser currentUser = defaultUser();
        QuestResponse response = mock(QuestResponse.class);

        when(questService.getQuests(1L)).thenReturn(List.of(response));

        List<QuestResponse> result = controller.getQuests(currentUser);

        assertThat(result).containsExactly(response);
        verify(questService).getQuests(1L);
    }

    @Test
    @DisplayName("claimQuest debe delegar en el service usando questId y usuario autenticado")
    void claimQuest_shouldDelegateToService() {
        QuestService questService = mock(QuestService.class);
        QuestController controller = new QuestController(questService);

        AuthenticatedUser currentUser = defaultUser();
        QuestClaimResponse response = mock(QuestClaimResponse.class);

        when(questService.claimQuest(1L, 1L)).thenReturn(response);

        QuestClaimResponse result = controller.claimQuest(currentUser, 1L);

        assertThat(result).isSameAs(response);
        verify(questService).claimQuest(1L, 1L);
    }
}
