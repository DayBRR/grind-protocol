package com.davidrr.grindprotocol.integration.quest;

import com.davidrr.grindprotocol.integration.AbstractPostgresIT;
import com.davidrr.grindprotocol.quest.enums.QuestFrequency;
import com.davidrr.grindprotocol.quest.enums.QuestStatus;
import com.davidrr.grindprotocol.quest.enums.QuestType;
import com.davidrr.grindprotocol.quest.model.Quest;
import com.davidrr.grindprotocol.quest.model.UserQuest;
import com.davidrr.grindprotocol.quest.repository.QuestRepository;
import com.davidrr.grindprotocol.quest.repository.UserQuestRepository;
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

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class QuestFlowIT extends AbstractPostgresIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private QuestRepository questRepository;
    @Autowired private UserQuestRepository userQuestRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserProfileRepository userProfileRepository;

    @Test
    @DisplayName("Flujo completo: crear quest, completar tareas, evaluar y reclamar recompensa")
    void fullFlow_shouldCompleteEvaluateAndClaimQuest() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        User user = userRepository.findByUsername(auth.username()).orElseThrow();

        UserProfile userProfile = userProfileRepository.findByUserId(user.getId()).orElseThrow();
        userProfile.setCorePoints(10L);
        userProfileRepository.save(userProfile);

        Quest quest = new Quest();
        quest.setCode("DAILY_3_TASKS");
        quest.setName("Daily Grinder");
        quest.setDescription("Complete 3 tasks today.");
        quest.setType(QuestType.TASK_COMPLETION_COUNT);
        quest.setFrequency(QuestFrequency.DAILY);
        quest.setTargetValue(3L);
        quest.setXpReward(50L);
        quest.setCorePointsReward(5L);
        quest.setEnabled(true);

        Quest savedQuest = questRepository.save(quest);

        mockMvc.perform(get("/me/quests")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].questId").value(savedQuest.getId()))
                .andExpect(jsonPath("$[0].code").value("DAILY_3_TASKS"))
                .andExpect(jsonPath("$[0].progressValue").value(0))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        long task1Id = createTask(auth.token(), "Entrenar");
        long task2Id = createTask(auth.token(), "Estudiar");
        long task3Id = createTask(auth.token(), "Leer");

        completeTask(auth.token(), task1Id);
        completeTask(auth.token(), task2Id);
        completeTask(auth.token(), task3Id);

        mockMvc.perform(post("/me/quests/evaluate")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/me/quests")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].questId").value(savedQuest.getId()))
                .andExpect(jsonPath("$[0].progressValue").value(3))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].periodStart").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$[0].periodEnd").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$[0].completedAt").isNotEmpty());

        mockMvc.perform(post("/me/quests/{questId}/claim", savedQuest.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questId").value(savedQuest.getId()))
                .andExpect(jsonPath("$.code").value("DAILY_3_TASKS"))
                .andExpect(jsonPath("$.name").value("Daily Grinder"))
                .andExpect(jsonPath("$.xpReward").value(50))
                .andExpect(jsonPath("$.corePointsReward").value(5))
                .andExpect(jsonPath("$.status").value("CLAIMED"));

        UserProfile updatedProfile = userProfileRepository.findByUserId(user.getId()).orElseThrow();

        assertThat(updatedProfile.getTotalXp()).isEqualTo(110L);
        assertThat(updatedProfile.getCorePoints()).isEqualTo(21L);

        Optional<UserQuest> userQuestOpt =
                userQuestRepository.findByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
                        user.getId(),
                        savedQuest.getId(),
                        LocalDate.now(),
                        LocalDate.now()
                );

        assertThat(userQuestOpt).isPresent();

        UserQuest userQuest = userQuestOpt.get();

        assertThat(userQuest.getProgressValue()).isEqualTo(3L);
        assertThat(userQuest.getStatus()).isEqualTo(QuestStatus.CLAIMED);
        assertThat(userQuest.getCompletedAt()).isNotNull();
        assertThat(userQuest.getClaimedAt()).isNotNull();
    }

    private long createTask(String token, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/me/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "Quest flow task",
                                  "category": "WORK",
                                  "difficulty": "EASY",
                                  "taskType": "DAILY",
                                  "baseXp": 20,
                                  "mandatory": false,
                                  "streakEligible": true,
                                  "repeatable": false,
                                  "maxCompletionsPerDay": 1,
                                  "diminishingReturnsEnabled": false,
                                  "active": true,
                                  "traitCodes": []
                                }
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private void completeTask(String token, long taskId) throws Exception {
        mockMvc.perform(post("/me/task-completions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": %d,
                                  "notes": "Quest flow completion"
                                }
                                """.formatted(taskId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").value(taskId));
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
                        .header(HttpHeaders.USER_AGENT, "JUnit-QuestFlowIT")
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
