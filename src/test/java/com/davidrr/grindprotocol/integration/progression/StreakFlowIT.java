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

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class StreakFlowIT extends AbstractPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    @DisplayName("Debe finalizar el día y actualizar currentStreak y bestStreak cuando el día califica")
    void finalizeDay_shouldUpdateStreakWhenDayQualified() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        long task1Id = createTask(auth.token(), """
                {
                  "title": "Entrenar",
                  "description": "Rutina de fuerza",
                  "category": "WORK",
                  "difficulty": "MEDIUM",
                  "taskType": "DAILY",
                  "baseXp": 20,
                  "mandatory": true,
                  "streakEligible": true,
                  "repeatable": false,
                  "maxCompletionsPerDay": 1,
                  "diminishingReturnsEnabled": false,
                  "active": true,
                  "traitCodes": []
                }
                """);

        long task2Id = createTask(auth.token(), """
                {
                  "title": "Estudiar",
                  "description": "Repasar Spring",
                  "category": "WORK",
                  "difficulty": "HARD",
                  "taskType": "DAILY",
                  "baseXp": 30,
                  "mandatory": true,
                  "streakEligible": true,
                  "repeatable": false,
                  "maxCompletionsPerDay": 1,
                  "diminishingReturnsEnabled": false,
                  "active": true,
                  "traitCodes": []
                }
                """);

        long task3Id = createTask(auth.token(), """
                {
                  "title": "Leer",
                  "description": "Leer 10 páginas",
                  "category": "WORK",
                  "difficulty": "EASY",
                  "taskType": "DAILY",
                  "baseXp": 40,
                  "mandatory": false,
                  "streakEligible": true,
                  "repeatable": false,
                  "maxCompletionsPerDay": 1,
                  "diminishingReturnsEnabled": false,
                  "active": true,
                  "traitCodes": []
                }
                """);

        completeTask(auth.token(), task1Id, "Completada 1");
        completeTask(auth.token(), task2Id, "Completada 2");
        completeTask(auth.token(), task3Id, "Completada 3");

        mockMvc.perform(get("/me/daily-progress/today")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayQualified").value(true));

        mockMvc.perform(post("/me/progression/finalize-day")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStreak").value(1))
                .andExpect(jsonPath("$.bestStreak").value(1))
                .andExpect(jsonPath("$.lastEvaluatedDate").value(LocalDate.now().toString()));

        Optional<User> userOpt = userRepository.findByUsername(auth.username());
        assertThat(userOpt).isPresent();

        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(userOpt.get().getId());
        assertThat(profileOpt).isPresent();

        UserProfile profile = profileOpt.get();
        assertThat(profile.getCurrentStreak()).isEqualTo(1);
        assertThat(profile.getBestStreak()).isEqualTo(1);
        assertThat(profile.getLastEvaluatedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Debe devolver 404 al finalizar un día sin DailyProgress")
    void finalizeDay_shouldReturnNotFoundWhenDailyProgressDoesNotExist() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        mockMvc.perform(post("/me/progression/finalize-day")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token()))
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("No debe duplicar la racha si se finaliza el mismo día dos veces")
    void finalizeDay_shouldNotDuplicateStreakWhenSameDayIsEvaluatedTwice() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        long task1Id = createTask(auth.token(), buildTaskJson("Tarea 1", 20, true));
        long task2Id = createTask(auth.token(), buildTaskJson("Tarea 2", 30, true));
        long task3Id = createTask(auth.token(), buildTaskJson("Tarea 3", 40, false));

        completeTask(auth.token(), task1Id, "Completada 1");
        completeTask(auth.token(), task2Id, "Completada 2");
        completeTask(auth.token(), task3Id, "Completada 3");

        mockMvc.perform(post("/me/progression/finalize-day")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/me/progression/finalize-day")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStreak").value(1))
                .andExpect(jsonPath("$.bestStreak").value(1));
    }

    private long createTask(String token, String requestBody) throws Exception {
        MvcResult result = mockMvc.perform(post("/me/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private void completeTask(String token, long taskId, String notes) throws Exception {
        mockMvc.perform(post("/me/task-completions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": %d,
                                  "notes": "%s"
                                }
                                """.formatted(taskId, notes)))
                .andExpect(status().isCreated());
    }

    private String buildTaskJson(String title, int baseXp, boolean mandatory) {
        return """
                {
                  "title": "%s",
                  "description": "Tarea de test",
                  "category": "WORK",
                  "difficulty": "MEDIUM",
                  "taskType": "DAILY",
                  "baseXp": %d,
                  "mandatory": %s,
                  "streakEligible": true,
                  "repeatable": false,
                  "maxCompletionsPerDay": 1,
                  "diminishingReturnsEnabled": false,
                  "active": true,
                  "traitCodes": []
                }
                """.formatted(title, baseXp, mandatory);
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
                        .header(HttpHeaders.USER_AGENT, "JUnit-StreakFlowIT")
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