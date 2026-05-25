package com.davidrr.grindprotocol.integration.achievement;

import com.davidrr.grindprotocol.achievement.enums.AchievementType;
import com.davidrr.grindprotocol.achievement.model.Achievement;
import com.davidrr.grindprotocol.achievement.model.UserAchievement;
import com.davidrr.grindprotocol.achievement.repository.AchievementRepository;
import com.davidrr.grindprotocol.achievement.repository.UserAchievementRepository;
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
class AchievementFlowIT extends AbstractPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    @DisplayName("Flujo completo: evaluar achievement, desbloquearlo y reclamar recompensa")
    void fullFlow_shouldEvaluateUnlockAndClaimAchievement() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        User user = userRepository.findByUsername(auth.username())
                .orElseThrow();

        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow();

        userProfile.setTotalXp(150L);
        userProfile.setCorePoints(10L);
        userProfileRepository.save(userProfile);

        Achievement achievement = new Achievement();
        achievement.setCode("TOTAL_XP_100");
        achievement.setName("Getting Started");
        achievement.setDescription("Reach 100 total XP.");
        achievement.setType(AchievementType.TOTAL_XP);
        achievement.setTargetValue(100L);
        achievement.setXpReward(25L);
        achievement.setCorePointsReward(5L);
        achievement.setEnabled(true);
        achievement.setHidden(false);

        Achievement savedAchievement = achievementRepository.save(achievement);

        mockMvc.perform(get("/me/achievements")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].achievementId").value(savedAchievement.getId()))
                .andExpect(jsonPath("$[0].code").value("TOTAL_XP_100"))
                .andExpect(jsonPath("$[0].progressValue").value(0))
                .andExpect(jsonPath("$[0].targetValue").value(100))
                .andExpect(jsonPath("$[0].unlocked").value(false))
                .andExpect(jsonPath("$[0].claimed").value(false));

        mockMvc.perform(post("/me/achievements/evaluate")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/me/achievements")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].achievementId").value(savedAchievement.getId()))
                .andExpect(jsonPath("$[0].progressValue").value(150))
                .andExpect(jsonPath("$[0].unlocked").value(true))
                .andExpect(jsonPath("$[0].unlockedAt").isNotEmpty())
                .andExpect(jsonPath("$[0].claimed").value(false));

        mockMvc.perform(post("/me/achievements/{achievementId}/claim", savedAchievement.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.achievementId").value(savedAchievement.getId()))
                .andExpect(jsonPath("$.code").value("TOTAL_XP_100"))
                .andExpect(jsonPath("$.name").value("Getting Started"))
                .andExpect(jsonPath("$.xpReward").value(25))
                .andExpect(jsonPath("$.corePointsReward").value(5))
                .andExpect(jsonPath("$.claimed").value(true));

        UserProfile updatedProfile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow();

        assertThat(updatedProfile.getTotalXp()).isEqualTo(175L);
        assertThat(updatedProfile.getCorePoints()).isEqualTo(15L);

        Optional<UserAchievement> userAchievementOpt =
                userAchievementRepository.findByUserProfileUserIdAndAchievementId(
                        user.getId(),
                        savedAchievement.getId()
                );

        assertThat(userAchievementOpt).isPresent();

        UserAchievement userAchievement = userAchievementOpt.get();

        assertThat(userAchievement.getProgressValue()).isEqualTo(150L);
        assertThat(userAchievement.getUnlocked()).isTrue();
        assertThat(userAchievement.getUnlockedAt()).isNotNull();
        assertThat(userAchievement.getClaimed()).isTrue();
        assertThat(userAchievement.getClaimedAt()).isNotNull();
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
                        .header(HttpHeaders.USER_AGENT, "JUnit-AchievementFlowIT")
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