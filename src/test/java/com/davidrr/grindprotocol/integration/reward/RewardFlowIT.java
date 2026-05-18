package com.davidrr.grindprotocol.integration.reward;

import com.davidrr.grindprotocol.integration.AbstractPostgresIT;
import com.davidrr.grindprotocol.reward.model.Reward;
import com.davidrr.grindprotocol.reward.model.RewardRedemption;
import com.davidrr.grindprotocol.reward.repository.RewardRedemptionRepository;
import com.davidrr.grindprotocol.reward.repository.RewardRepository;
import com.davidrr.grindprotocol.reward.enums.RewardCategory;
import com.davidrr.grindprotocol.reward.enums.RewardType;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class RewardFlowIT extends AbstractPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private RewardRedemptionRepository rewardRedemptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    @DisplayName("Flujo completo: ver rewards y canjear reward")
    void fullFlow_shouldRedeemRewardAndSpendCorePoints() throws Exception {

        AuthContext auth = registerAndGetAuthContext();

        Optional<User> userOpt = userRepository.findByUsername(auth.username());
        assertThat(userOpt).isPresent();

        Long userId = userOpt.get().getId();

        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow();

        userProfile.setCorePoints(100L);

        userProfileRepository.save(userProfile);

        Reward reward = new Reward();

        reward.setName("2 horas de gaming");
        reward.setDescription("Tiempo para jugar videojuegos");
        reward.setType(RewardType.REAL);
        reward.setCategory(RewardCategory.GAMING);
        reward.setCostCorePoints(20L);
        reward.setEnabled(true);
        reward.setRepeatable(true);

        Reward savedReward = rewardRepository.save(reward);

        mockMvc.perform(get("/me/rewards")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("2 horas de gaming"))
                .andExpect(jsonPath("$[0].costCorePoints").value(20));

        mockMvc.perform(post("/me/rewards/{rewardId}/redeem", savedReward.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rewardId").value(savedReward.getId()))
                .andExpect(jsonPath("$.rewardName").value("2 horas de gaming"))
                .andExpect(jsonPath("$.costPaid").value(20))
                .andExpect(jsonPath("$.remainingCorePoints").value(80))
                .andExpect(jsonPath("$.status").value("REDEEMED"));

        UserProfile updatedProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow();

        assertThat(updatedProfile.getCorePoints()).isEqualTo(80L);

        List<RewardRedemption> redemptions =
                rewardRedemptionRepository.findAll();

        assertThat(redemptions).hasSize(1);

        RewardRedemption redemption = redemptions.get(0);

        assertThat(redemption.getReward().getId())
                .isEqualTo(savedReward.getId());

        assertThat(redemption.getUserProfile().getId())
                .isEqualTo(userProfile.getId());

        assertThat(redemption.getCostPaid()).isEqualTo(20L);

        assertThat(redemption.getRedeemedAt()).isNotNull();
    }

    private AuthContext registerAndGetAuthContext() throws Exception {

        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, 8);

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
                        .header(HttpHeaders.USER_AGENT, "JUnit-RewardFlowIT")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode json = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        String token = json.get("token").asText();

        return new AuthContext(
                username,
                email,
                password,
                token
        );
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record AuthContext(
            String username,
            String email,
            String password,
            String token
    ) {
    }
}