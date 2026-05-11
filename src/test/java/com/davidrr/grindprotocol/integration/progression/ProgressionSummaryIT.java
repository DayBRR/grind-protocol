package com.davidrr.grindprotocol.integration.progression;

import com.davidrr.grindprotocol.integration.AbstractPostgresIT;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.user.repository.UserRepository;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ProgressionSummaryIT extends AbstractPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    @DisplayName("GET /me/progression debe devolver el resumen de progresión del usuario autenticado")
    void getProgressionSummary_shouldReturnAuthenticatedUserProgressionSummary() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        Optional<User> userOpt = userRepository.findByUsername(auth.username());
        assertThat(userOpt).isPresent();

        User user = userOpt.get();

        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElseThrow();
        profile.setTotalXp(250L);
        profile.setLevel(3);
        profile.setCorePoints(25L);
        profile.setCurrentStreak(4);
        profile.setBestStreak(7);
        userProfileRepository.save(profile);

        mockMvc.perform(get("/me/progression")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalXp").value(250))
                .andExpect(jsonPath("$.level").value(3))
                .andExpect(jsonPath("$.xpForCurrentLevel").value(200))
                .andExpect(jsonPath("$.xpForNextLevel").value(300))
                .andExpect(jsonPath("$.xpProgressInCurrentLevel").value(50))
                .andExpect(jsonPath("$.xpRemainingForNextLevel").value(50))
                .andExpect(jsonPath("$.corePoints").value(25))
                .andExpect(jsonPath("$.currentStreak").value(4))
                .andExpect(jsonPath("$.bestStreak").value(7));
    }

    @Test
    @DisplayName("GET /me/progression debe devolver valores iniciales tras registrar usuario")
    void getProgressionSummary_shouldReturnInitialProgressionAfterRegister() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        mockMvc.perform(get("/me/progression")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalXp").value(0))
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.xpForCurrentLevel").value(0))
                .andExpect(jsonPath("$.xpForNextLevel").value(100))
                .andExpect(jsonPath("$.xpProgressInCurrentLevel").value(0))
                .andExpect(jsonPath("$.xpRemainingForNextLevel").value(100))
                .andExpect(jsonPath("$.corePoints").value(0))
                .andExpect(jsonPath("$.currentStreak").value(0))
                .andExpect(jsonPath("$.bestStreak").value(0));
    }

    @Test
    @DisplayName("GET /me/progression debe devolver 401 sin token")
    void getProgressionSummary_shouldReturnUnauthorizedWhenNoToken() throws Exception {
        mockMvc.perform(get("/me/progression"))
                .andExpect(status().isUnauthorized());
    }

    private AuthContext registerAndGetAuthContext() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "user_" + suffix;
        String email = username + "@test.com";
        String password = "Password123!";

        String requestBody = """
                {
                  "username": "%s",
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(username, email, password);

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.USER_AGENT, "JUnit-ProgressionSummaryIT")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = json.get("token").asText();

        return new AuthContext(username, email, password, token);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record AuthContext(String username, String email, String password, String token) {
    }
}