package com.davidrr.grindprotocol.progression.controller;

import com.davidrr.grindprotocol.progression.service.StreakService;
import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import com.davidrr.grindprotocol.utils.TestAuthenticatedUserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProgressionControllerTest {

    @Mock
    private StreakService streakService;

    @InjectMocks
    private ProgressionController progressionController;

    @Test
    @DisplayName("finalizeDay debe delegar en StreakService usando la fecha recibida")
    void finalizeDay_shouldDelegateToStreakServiceWithProvidedDate() {
        AuthenticatedUser currentUser = TestAuthenticatedUserFactory.defaultUser();
        LocalDate date = LocalDate.of(2026, 4, 18);

        progressionController.finalizeDay(currentUser, date);

        verify(streakService).finalizeDay(1L, date);
    }

    @Test
    @DisplayName("finalizeDay debe usar LocalDate.now cuando no se informa fecha")
    void finalizeDay_shouldUseCurrentDateWhenDateIsNull() {
        AuthenticatedUser currentUser = TestAuthenticatedUserFactory.defaultUser();
        LocalDate today = LocalDate.now();

        progressionController.finalizeDay(currentUser, null);

        verify(streakService).finalizeDay(1L, today);
    }
}