package com.davidrr.grindprotocol.security.auth.controller;

import com.davidrr.grindprotocol.user.repository.RefreshTokenRepository;
import com.davidrr.grindprotocol.user.repository.UserRepository;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import com.davidrr.security.audit.SecurityAuditService;
import com.davidrr.security.audit.SecurityEventType;
import com.davidrr.security.auth.event.UserRegisteredEvent;
import com.davidrr.security.auth.session.service.RefreshTokenService;
import com.davidrr.security.jwt.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Shared HTTP and persistence contract, exercised with H2 and an isolated PostgreSQL container. */
@SpringBootTest
@AutoConfigureMockMvc
@RecordApplicationEvents
public abstract class RegistrationContractTests {
    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper mapper;
    @Autowired protected ApplicationEvents events;
    @MockitoSpyBean protected UserRepository users;
    @Autowired protected UserProfileRepository profiles;
    @Autowired protected RefreshTokenRepository sessions;
    @MockitoSpyBean protected JwtService jwt;
    @MockitoSpyBean protected RefreshTokenService refresh;
    @MockitoSpyBean protected SecurityAuditService audit;

    @Test
    void duplicateUsernameReturns409WithoutSideEffects() throws Exception {
        String name = uniqueName();
        register(name, name + "@example.test").andExpect(status().isOk());
        long[] before = counts();
        resetObservations();
        assertConflict(name, "unused-" + name + "@example.test", before);
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void duplicateEmailReturns409WithoutSideEffects() throws Exception {
        String name = uniqueName();
        register(name, name + "@example.test").andExpect(status().isOk());
        long[] before = counts();
        resetObservations();
        assertConflict("unused-" + name, name + "@example.test", before);
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void successfulRegistrationStillCreatesExactlyOneUserProfileSessionAndEvent() throws Exception {
        String name = uniqueName();
        long[] before = counts();
        resetObservations();
        register(name, name + "@example.test")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true));
        long[] after = counts();
        for (int i = 0; i < before.length; i++) assertThat(after[i]).isEqualTo(before[i] + 1);
        assertThat(events.stream(UserRegisteredEvent.class).count()).isEqualTo(1);
        verify(jwt).generateToken(any());
        verify(refresh).createRefreshToken(eq(name), any());
        verify(audit).log(eq(SecurityEventType.REGISTER_SUCCESS), any());
        assertThat(profiles.findByUserId(users.findByUsername(name).orElseThrow().getId())).isPresent();
    }

    protected void assertConflict(String username, String email, long[] before) throws Exception {
        var sqlLogger = (Logger) LoggerFactory.getLogger("org.hibernate.engine.jdbc.spi.SqlExceptionHelper");
        var apiLogger = (Logger) LoggerFactory.getLogger(
                "com.davidrr.grindprotocol.common.exception.DomainExceptionHandler");
        var logs = new ListAppender<ILoggingEvent>();
        logs.start();
        sqlLogger.addAppender(logs);
        apiLogger.addAppender(logs);
        String body;
        try {
            body = register(username, email)
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.code").value("USER_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value(
                        "Ya existe un usuario con ese nombre de usuario o correo electrónico"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        } finally {
            sqlLogger.detachAppender(logs);
            apiLogger.detachAppender(logs);
            logs.stop();
        }
        assertThat(logs.list).isNotEmpty().allSatisfy(event ->
                assertThat(event.getFormattedMessage()).doesNotContain(username, email, "Password123!"));
        JsonNode json = mapper.readTree(body);
        Set<String> fields = new HashSet<>();
        json.fieldNames().forEachRemaining(fields::add);
        assertThat(fields).containsExactlyInAnyOrder("timestamp", "status", "error", "code", "message", "path");
        assertThat(Instant.parse(json.get("timestamp").asText())).isNotNull();
        assertThat(body).doesNotContain(username, email, "Password123!", "SQL", "Exception");
        assertThat(counts()).containsExactly(before);
        assertThat(events.stream(UserRegisteredEvent.class).count()).isZero();
        verify(jwt, never()).generateToken(any());
        verify(refresh, never()).createRefreshToken(anyString(), any());
        verify(audit, never()).log(eq(SecurityEventType.REGISTER_SUCCESS), any());
    }

    protected ResultActions register(String username, String email) throws Exception {
        return mvc.perform(post("/api/auth/register").contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(java.util.Map.of(
                        "username", username, "email", email, "password", "Password123!"))));
    }

    protected long[] counts() {
        return new long[]{users.count(), profiles.count(), sessions.count()};
    }

    protected void resetObservations() {
        events.clear();
        clearInvocations(users, jwt, refresh, audit);
    }

    protected String uniqueName() {
        return "qa_" + UUID.randomUUID().toString().replace("-", "");
    }
}
