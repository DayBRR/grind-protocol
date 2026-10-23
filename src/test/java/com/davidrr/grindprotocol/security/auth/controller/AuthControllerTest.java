package com.davidrr.grindprotocol.security.auth.controller;

import com.davidrr.grindprotocol.common.exception.DomainExceptionHandler;
import com.davidrr.grindprotocol.common.exception.SecurityExceptionHandler;
import com.davidrr.security.audit.SecurityAuditContext;
import com.davidrr.security.audit.SecurityAuditService;
import com.davidrr.security.audit.SecurityEventType;
import com.davidrr.security.auth.dto.SessionFingerprint;
import com.davidrr.security.auth.port.*;
import com.davidrr.security.auth.service.impl.AuthServiceImpl;
import com.davidrr.security.auth.session.service.RefreshTokenService;
import com.davidrr.security.common.GlobalExceptionHandler;
import com.davidrr.security.jwt.JwtService;
import com.davidrr.security.ratelimit.exception.RateLimitExceededException;
import com.davidrr.security.ratelimit.service.RateLimiterService;
import com.davidrr.security.ratelimit.util.RateLimitKeyFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {
    private final JwtService jwt = mock(JwtService.class);
    private final RefreshTokenService refresh = mock(RefreshTokenService.class);
    private final RateLimiterService limiter = mock(RateLimiterService.class);
    private final RateLimitKeyFactory keys = mock(RateLimitKeyFactory.class);
    private final SecurityAuditService audit = mock(SecurityAuditService.class);
    private final UserLoginAuditPort loginAudit = mock(UserLoginAuditPort.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        var encoder = new BCryptPasswordEncoder(4);
        UserDetails user = User.withUsername("existing").password(encoder.encode("correct-password"))
                .roles("USER").build();
        var provider = new DaoAuthenticationProvider(username -> {
            if (username.equals("existing")) return user;
            throw new UsernameNotFoundException("Internal detail: user does not exist");
        });
        provider.setPasswordEncoder(encoder);
        var service = new AuthServiceImpl(new ProviderManager(provider), jwt, refresh,
                mock(UserProvider.class), limiter, keys, audit, mock(UserRegistrationPort.class),
                mock(ApplicationEventPublisher.class), encoder, loginAudit);
        var controller = new AuthController(service);
        ReflectionTestUtils.setField(controller, "refreshCookieName", "refresh_token");
        ReflectionTestUtils.setField(controller, "cookiePath", "/auth");
        ReflectionTestUtils.setField(controller, "cookieSameSite", "Lax");
        ReflectionTestUtils.setField(controller, "cookieMaxAgeDays", 14L);
        when(keys.loginKey(any(SessionFingerprint.class))).thenReturn("test-login");
        // Deliberately register the generic advice first; @Order must determine precedence.
        var mapper = new ObjectMapper().findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).setControllerAdvice(
                new DomainExceptionHandler(), new GlobalExceptionHandler(), new SecurityExceptionHandler()).build();
    }

    @Test
    void wrongPasswordAndMissingUserHaveSameSafeContractAndPreserveAudit() throws Exception {
        String first = assertBadCredentials("existing");
        String second = assertBadCredentials("missing");
        var mapper = new ObjectMapper();
        assertThat(mapper.readTree(first).get("message")).isEqualTo(mapper.readTree(second).get("message"));
        var contexts = ArgumentCaptor.forClass(SecurityAuditContext.class);
        verify(audit, times(2)).log(eq(SecurityEventType.LOGIN_FAILURE), contexts.capture());
        assertThat(contexts.getAllValues()).allSatisfy(context -> {
            assertThat(context.reason()).isEqualTo("BAD_CREDENTIALS");
            assertThat(context.ip()).isEqualTo("127.0.0.1");
            assertThat(context.userAgent()).isEqualTo("JUnit");
        });
        verify(limiter, times(2)).onFailure("login", "test-login");
        verify(limiter, never()).onSuccess(anyString(), anyString());
        verifyNoInteractions(jwt, refresh, loginAudit);
    }

    private String assertBadCredentials(String username) throws Exception {
        String body = mvc.perform(login(username, "wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("AUTH_BAD_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Credenciales erróneas"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        JsonNode json = new ObjectMapper().readTree(body);
        Set<String> fields = new HashSet<>();
        json.fieldNames().forEachRemaining(fields::add);
        assertThat(fields).containsExactlyInAnyOrder("timestamp", "status", "error", "code", "message", "path");
        assertThat(Instant.parse(json.get("timestamp").asText())).isNotNull();
        assertThat(body).doesNotContain(username, "wrong-password", "BadCredentialsException", "Internal detail");
        return body;
    }

    @Test
    void correctLoginReturnsTokensCookieAndSuccessAudit() throws Exception {
        when(jwt.generateToken(any(UserDetails.class))).thenReturn("access-token");
        when(refresh.createRefreshToken(eq("existing"), any())).thenReturn("refresh-secret");
        mvc.perform(login("existing", "correct-password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=refresh-secret")))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().secure("refresh_token", false))
                .andExpect(cookie().path("refresh_token", "/auth"))
                .andExpect(cookie().maxAge("refresh_token", 14 * 24 * 60 * 60))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")));
        verify(audit).log(eq(SecurityEventType.LOGIN_SUCCESS), any());
        verify(audit, never()).log(eq(SecurityEventType.LOGIN_FAILURE), any());
        verify(limiter).onSuccess("login", "test-login");
        verify(loginAudit).recordSuccessfulLogin("existing");
    }

    @Test
    void unsupportedGetReturns405AndAllowWithoutAttemptingAuthentication() throws Exception {
        mvc.perform(get("/api/auth/login").contextPath("/api"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(HttpHeaders.ALLOW, "POST"))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"))
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message").value("Método HTTP no permitido"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
        verifyNoInteractions(limiter, audit, jwt, refresh, loginAudit);
    }

    @Test
    void validationRetainsApplicationFieldDetails() throws Exception {
        mvc.perform(login("", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.length()").value(2));
        verifyNoInteractions(limiter, audit);
    }

    @Test
    void missingRefreshRetains401() throws Exception {
        mvc.perform(post("/api/auth/refresh").contextPath("/api"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_REFRESH_MISSING"));
    }

    @Test
    void rateLimitRetains429() throws Exception {
        doThrow(new RateLimitExceededException()).when(limiter).check("login", "test-login");
        mvc.perform(login("existing", "correct-password"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("AUTH_RATE_LIMIT_EXCEEDED"));
        verify(audit).log(eq(SecurityEventType.RATE_LIMIT_EXCEEDED), any());
        verifyNoInteractions(jwt, refresh, loginAudit);
    }

    @Test
    void unexpectedFailureRetainsSafe500() throws Exception {
        doThrow(new IllegalStateException("Internal database detail")).when(limiter).check("login", "test-login");
        mvc.perform(login("existing", "correct-password"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Se ha producido un error interno"));
    }

    private MockHttpServletRequestBuilder login(String username, String password) {
        return post("/api/auth/login").contextPath("/api")
                .header(HttpHeaders.USER_AGENT, "JUnit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}");
    }
}
