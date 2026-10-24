package com.davidrr.grindprotocol.security.auth.controller;

import com.davidrr.security.auth.service.AuthService;
import com.davidrr.security.auth.dto.AuthTokens;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Exercises actual component scanning, advice ordering and the security filter chain using H2. */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthLoginSecurityTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void loginSetsRefreshCookieForApiAuthPath() throws Exception {
        when(authService.login(any(), any())).thenReturn(new AuthTokens("access", "login-refresh"));
        assertRefreshCookie(mvc.perform(post("/api/auth/login").contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"password\":\"password123\"}")), "login-refresh", 14 * 24 * 60 * 60)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access"));
    }

    @Test
    void registrationSetsRefreshCookieForApiAuthPath() throws Exception {
        when(authService.register(any(), any())).thenReturn(new AuthTokens("access", "register-refresh"));
        assertRefreshCookie(mvc.perform(post("/api/auth/register").contextPath("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user\",\"email\":\"user@example.test\",\"password\":\"password123\"}")),
                "register-refresh", 14 * 24 * 60 * 60)
                .andExpect(status().isOk());
    }

    @Test
    void refreshReplacesCookieAtTheSameApiAuthPath() throws Exception {
        when(authService.refresh(eq("old-refresh"), any())).thenReturn(new AuthTokens("new-access", "rotated-refresh"));
        assertRefreshCookie(mvc.perform(post("/api/auth/refresh").contextPath("/api")
                .cookie(new Cookie("refresh_token", "old-refresh"))), "rotated-refresh", 14 * 24 * 60 * 60)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access"));
        verify(authService).refresh(eq("old-refresh"), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/logout", "/logout-all"})
    void logoutDeletesCookieAtTheSameApiAuthPath(String endpoint) throws Exception {
        assertRefreshCookie(mvc.perform(post("/api/auth" + endpoint).contextPath("/api")
                .cookie(new Cookie("refresh_token", "current-refresh"))), "", 0)
                .andExpect(status().isNoContent());
        if (endpoint.equals("/logout")) {
            verify(authService).logout("current-refresh");
        } else {
            verify(authService).logoutAll("current-refresh");
        }
    }

    private ResultActions assertRefreshCookie(ResultActions result, String value, int maxAge) throws Exception {
        return result
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=" + value)))
                .andExpect(cookie().value("refresh_token", value))
                .andExpect(cookie().path("refresh_token", "/api/auth"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().secure("refresh_token", false))
                .andExpect(cookie().maxAge("refresh_token", maxAge))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")));
    }

    @Test
    void anonymousBadCredentialsReachTheSecurityAdvice() throws Exception {
        when(authService.login(any(), any())).thenThrow(new BadCredentialsException("Internal detail"));
        mvc.perform(post("/api/auth/login").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"incorrect\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_BAD_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Credenciales erróneas"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
        verify(authService).login(any(), any());
    }

    @Test
    void anonymousGetReachesMvcAndReturns405() throws Exception {
        mvc.perform(get("/api/auth/login").contextPath("/api"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(HttpHeaders.ALLOW, "POST"))
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
        verifyNoInteractions(authService);
    }

    @Test
    void validationStillUsesApplicationContractWithAllAdvicesRegistered() throws Exception {
        mvc.perform(post("/api/auth/login").contextPath("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.length()").value(2));
        verifyNoInteractions(authService);
    }
}
