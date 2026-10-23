package com.davidrr.grindprotocol.security.auth.controller;

import com.davidrr.security.auth.service.AuthService;
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

import static org.mockito.ArgumentMatchers.any;
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
