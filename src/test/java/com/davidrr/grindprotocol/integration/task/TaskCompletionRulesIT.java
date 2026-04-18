package com.davidrr.grindprotocol.integration.task;

import com.davidrr.grindprotocol.integration.AbstractPostgresIT;
import com.davidrr.grindprotocol.task.model.DailyProgress;
import com.davidrr.grindprotocol.task.model.Task;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class TaskCompletionRulesIT extends AbstractPostgresIT {

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
    @DisplayName("No debe permitir completar una tarea no repetible dos veces el mismo día")
    void shouldNotAllowCompletingNonRepeatableTaskTwiceSameDay() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        long taskId = createTask(auth.token(), """
                {
                  "title": "Entrenar",
                  "description": "Rutina única",
                  "category": "WORK",
                  "difficulty": "MEDIUM",
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
                """);

        completeTask(auth.token(), taskId, "Primera");

        mockMvc.perform(post("/me/task-completions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": %d,
                                  "notes": "Segunda"
                                }
                                """.formatted(taskId)))
                .andExpect(status().isBadRequest());

        Optional<User> userOpt = userRepository.findByUsername(auth.username());
        assertThat(userOpt).isPresent();

        assertThat(taskCompletionRepository.findByUserIdAndCompletionDate(userOpt.get().getId(), LocalDate.now()))
                .hasSize(1);
    }

    @Test
    @DisplayName("No debe permitir completar una tarea inactiva")
    void shouldNotAllowCompletingInactiveTask() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        long taskId = createTask(auth.token(), """
                {
                  "title": "Dormida",
                  "description": "Inactiva",
                  "category": "WORK",
                  "difficulty": "EASY",
                  "taskType": "DAILY",
                  "baseXp": 10,
                  "mandatory": false,
                  "streakEligible": true,
                  "repeatable": false,
                  "maxCompletionsPerDay": 1,
                  "diminishingReturnsEnabled": false,
                  "active": true,
                  "traitCodes": []
                }
                """);

        Optional<User> userOpt = userRepository.findByUsername(auth.username());
        assertThat(userOpt).isPresent();

        Task task = taskRepository.findWithTraitsByIdAndUserId(taskId, userOpt.get().getId()).orElseThrow();
        task.setActive(false);
        taskRepository.save(task);

        mockMvc.perform(post("/me/task-completions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": %d,
                                  "notes": "No debería poder"
                                }
                                """.formatted(taskId)))
                .andExpect(status().isBadRequest());

        assertThat(taskCompletionRepository.findByUserIdAndCompletionDate(userOpt.get().getId(), LocalDate.now()))
                .isEmpty();
    }

    @Test
    @DisplayName("No debe permitir completar una tarea de otro usuario")
    void shouldNotAllowCompletingAnotherUsersTask() throws Exception {
        AuthContext owner = registerAndGetAuthContext();
        AuthContext anotherUser = registerAndGetAuthContext();

        long ownerTaskId = createTask(owner.token(), """
                {
                  "title": "Privada",
                  "description": "Solo owner",
                  "category": "WORK",
                  "difficulty": "MEDIUM",
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
                """);

        mockMvc.perform(post("/me/task-completions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(anotherUser.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": %d,
                                  "notes": "No debería poder"
                                }
                                """.formatted(ownerTaskId)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("No debe permitir superar maxCompletionsPerDay en tarea repetible")
    void shouldNotAllowExceedingMaxCompletionsPerDay() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        long taskId = createTask(auth.token(), """
                {
                  "title": "Leer",
                  "description": "Repetible con límite",
                  "category": "WORK",
                  "difficulty": "EASY",
                  "taskType": "DAILY",
                  "baseXp": 21,
                  "mandatory": false,
                  "streakEligible": true,
                  "repeatable": true,
                  "maxCompletionsPerDay": 2,
                  "diminishingReturnsEnabled": true,
                  "active": true,
                  "traitCodes": []
                }
                """);

        completeTask(auth.token(), taskId, "Primera");
        completeTask(auth.token(), taskId, "Segunda");

        mockMvc.perform(post("/me/task-completions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId": %d,
                                  "notes": "Tercera"
                                }
                                """.formatted(taskId)))
                .andExpect(status().isBadRequest());

        Optional<User> userOpt = userRepository.findByUsername(auth.username());
        assertThat(userOpt).isPresent();

        assertThat(taskCompletionRepository.findByUserIdAndCompletionDate(userOpt.get().getId(), LocalDate.now()))
                .hasSize(2);
    }

    @Test
    @DisplayName("El día no debe calificar si falta una tarea obligatoria aunque se alcance el mínimo")
    void dayShouldNotQualifyWhenMandatoryTaskMissing() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        long mandatoryTask1 = createTask(auth.token(), """
                {
                  "title": "Obligatoria 1",
                  "description": "Primera obligatoria",
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

        long mandatoryTask2 = createTask(auth.token(), """
                {
                  "title": "Obligatoria 2",
                  "description": "Segunda obligatoria",
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

        long optionalTask1 = createTask(auth.token(), """
                {
                  "title": "Opcional 1",
                  "description": "Opcional",
                  "category": "WORK",
                  "difficulty": "EASY",
                  "taskType": "DAILY",
                  "baseXp": 10,
                  "mandatory": false,
                  "streakEligible": true,
                  "repeatable": false,
                  "maxCompletionsPerDay": 1,
                  "diminishingReturnsEnabled": false,
                  "active": true,
                  "traitCodes": []
                }
                """);

        long optionalTask2 = createTask(auth.token(), """
                {
                  "title": "Opcional 2",
                  "description": "Opcional",
                  "category": "WORK",
                  "difficulty": "EASY",
                  "taskType": "DAILY",
                  "baseXp": 10,
                  "mandatory": false,
                  "streakEligible": true,
                  "repeatable": false,
                  "maxCompletionsPerDay": 1,
                  "diminishingReturnsEnabled": false,
                  "active": true,
                  "traitCodes": []
                }
                """);

        completeTask(auth.token(), mandatoryTask1, "Hecha");
        completeTask(auth.token(), optionalTask1, "Hecha");
        completeTask(auth.token(), optionalTask2, "Hecha");

        mockMvc.perform(get("/me/daily-progress/today")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiredTaskCount").value(3))
                .andExpect(jsonPath("$.completedValidTaskCount").value(3))
                .andExpect(jsonPath("$.mandatoryTasksRequired").value(2))
                .andExpect(jsonPath("$.mandatoryTasksCompleted").value(1))
                .andExpect(jsonPath("$.dayQualified").value(false));

        Optional<User> userOpt = userRepository.findByUsername(auth.username());
        assertThat(userOpt).isPresent();

        Optional<DailyProgress> progressOpt =
                dailyProgressRepository.findByUserIdAndProgressDate(userOpt.get().getId(), LocalDate.now());

        assertThat(progressOpt).isPresent();
        assertThat(progressOpt.get().isDayQualified()).isFalse();

        assertThat(mandatoryTask2).isNotNull();
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
                        .header(HttpHeaders.USER_AGENT, "JUnit-TaskCompletionRulesIT")
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