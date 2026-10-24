package com.davidrr.grindprotocol.integration.auth;

import com.davidrr.grindprotocol.integration.PostgresTestContainer;
import com.davidrr.grindprotocol.security.auth.controller.RegistrationContractTests;
import com.davidrr.grindprotocol.user.model.User;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("integration-test")
class AuthRegisterConflictIT extends RegistrationContractTests {
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> PostgresTestContainer.getInstance().getJdbcUrl());
        registry.add("spring.datasource.username", () -> PostgresTestContainer.getInstance().getUsername());
        registry.add("spring.datasource.password", () -> PostgresTestContainer.getInstance().getPassword());
        registry.add("spring.datasource.driver-class-name", () -> PostgresTestContainer.getInstance().getDriverClassName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"username", "email"})
    void stalePrechecksStillReturn409FromRealPostgresConstraintWithinTransaction(String duplicate) throws Exception {
        String name = uniqueName();
        String email = name + "@example.test";
        register(name, email).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        long[] before = counts();
        resetObservations();

        String requestedName = duplicate.equals("username") ? name : "unused-" + name;
        String requestedEmail = duplicate.equals("email") ? email : "unused-" + email;
        // Deterministically reproduce a concurrent winner: prechecks see no conflict,
        // but the real database already owns the requested unique value.
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            return false;
        }).when(users).existsByUsername(requestedName);
        doReturn(false).when(users).existsByEmail(requestedEmail);

        assertConflict(requestedName, requestedEmail, before);
        verify(users).saveAndFlush(any(User.class));
        // assertConflict queries all counts after the HTTP response: the failed
        // transaction must have rolled back, rather than failing at a later commit.
    }
}
