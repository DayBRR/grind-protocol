package com.davidrr.grindprotocol.integration.task;

import com.davidrr.grindprotocol.integration.AbstractPostgresIT;
import com.davidrr.grindprotocol.task.model.DailyProgress;
import com.davidrr.grindprotocol.task.model.Task;
import com.davidrr.grindprotocol.task.model.TaskCompletion;
import com.davidrr.grindprotocol.task.repository.DailyProgressRepository;
import com.davidrr.grindprotocol.task.repository.TaskCompletionRepository;
import com.davidrr.grindprotocol.task.repository.TaskRepository;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.user.repository.UserRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class TaskFlowIT extends AbstractPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskCompletionRepository taskCompletionRepository;

    @Autowired
    private DailyProgressRepository dailyProgressRepository;

    @Test
    @DisplayName("Flujo completo: crear tareas, completar 3 y calificar el día")
    void fullFlow_shouldCreateTasksCompleteThemAndQualifyDay() throws Exception {
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

        mockMvc.perform(post("/me/task-completions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": %d,
                                  "notes": "Primera completion"
                                }
                                """.formatted(task1Id)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").value(task1Id))
                .andExpect(jsonPath("$.countedForDailyGoal").value(true))
                .andExpect(jsonPath("$.countedForStreak").value(true))
                .andExpect(jsonPath("$.baseXp").value(20))
                .andExpect(jsonPath("$.awardedXp").value(20))
                .andExpect(jsonPath("$.awardedCorePoints").value(2))
                .andExpect(jsonPath("$.completionIndexForDay").value(1));

        mockMvc.perform(post("/me/task-completions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": %d,
                                  "notes": "Segunda completion"
                                }
                                """.formatted(task2Id)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").value(task2Id));

        mockMvc.perform(post("/me/task-completions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": %d,
                                  "notes": "Tercera completion"
                                }
                                """.formatted(task3Id)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").value(task3Id));

        mockMvc.perform(get("/me/task-completions/today")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(get("/me/daily-progress/today")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiredTaskCount").value(3))
                .andExpect(jsonPath("$.completedValidTaskCount").value(3))
                .andExpect(jsonPath("$.mandatoryTasksRequired").value(2))
                .andExpect(jsonPath("$.mandatoryTasksCompleted").value(2))
                .andExpect(jsonPath("$.dayQualified").value(true));

        Optional<User> userOpt = userRepository.findByUsername(auth.username());
        assertThat(userOpt).isPresent();

        Long userId = userOpt.get().getId();

        List<Task> tasks = taskRepository.findWithTraitsByUserIdAndActiveTrue(userId);
        assertThat(tasks).hasSize(3);

        List<TaskCompletion> completions = taskCompletionRepository.findByUserIdAndCompletionDate(userId, LocalDate.now());
        assertThat(completions).hasSize(3);

        Optional<DailyProgress> dailyProgress = dailyProgressRepository.findByUserIdAndProgressDate(userId, LocalDate.now());
        assertThat(dailyProgress).isPresent();
        assertThat(dailyProgress.get().isDayQualified()).isTrue();
    }

    @Test
    @DisplayName("Tarea repetible con diminishing returns: la segunda completion no cuenta para goal ni streak")
    void repeatableTask_shouldApplyDiminishingReturnsOnSecondCompletion() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        long taskId = createTask(auth.token(), """
                {
                  "title": "Leer",
                  "description": "Leer 10 páginas",
                  "category": "WORK",
                  "difficulty": "EASY",
                  "taskType": "DAILY",
                  "baseXp": 21,
                  "mandatory": false,
                  "streakEligible": true,
                  "repeatable": true,
                  "maxCompletionsPerDay": 3,
                  "diminishingReturnsEnabled": true,
                  "active": true,
                  "traitCodes": []
                }
                """);

        mockMvc.perform(post("/me/task-completions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": %d,
                                  "notes": "Primera"
                                }
                                """.formatted(taskId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.completionIndexForDay").value(1))
                .andExpect(jsonPath("$.countedForDailyGoal").value(true))
                .andExpect(jsonPath("$.countedForStreak").value(true))
                .andExpect(jsonPath("$.awardedXp").value(21))
                .andExpect(jsonPath("$.awardedCorePoints").value(2));

        mockMvc.perform(post("/me/task-completions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": %d,
                                  "notes": "Segunda"
                                }
                                """.formatted(taskId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.completionIndexForDay").value(2))
                .andExpect(jsonPath("$.countedForDailyGoal").value(false))
                .andExpect(jsonPath("$.countedForStreak").value(false))
                .andExpect(jsonPath("$.awardedXp").value(10))
                .andExpect(jsonPath("$.awardedCorePoints").value(1));

        mockMvc.perform(get("/me/daily-progress/today")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiredTaskCount").value(3))
                .andExpect(jsonPath("$.completedValidTaskCount").value(1))
                .andExpect(jsonPath("$.mandatoryTasksRequired").value(0))
                .andExpect(jsonPath("$.mandatoryTasksCompleted").value(0))
                .andExpect(jsonPath("$.dayQualified").value(false));
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
                        .header(HttpHeaders.USER_AGENT, "JUnit-TaskFlowIT")
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