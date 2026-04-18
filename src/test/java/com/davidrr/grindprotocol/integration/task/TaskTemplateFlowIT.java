package com.davidrr.grindprotocol.integration.task;

import com.davidrr.grindprotocol.integration.AbstractPostgresIT;
import com.davidrr.grindprotocol.task.model.Task;
import com.davidrr.grindprotocol.task.model.TaskTemplate;
import com.davidrr.grindprotocol.task.repository.TaskRepository;
import com.davidrr.grindprotocol.task.repository.TaskTemplateRepository;
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

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class TaskTemplateFlowIT extends AbstractPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskTemplateRepository taskTemplateRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    @DisplayName("Flujo completo: crear plantilla pública, consultarla y crear tarea desde plantilla")
    void fullFlow_shouldCreatePublicTemplateAndCreateTaskFromIt() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        long templateId = createTemplate(auth.token(), """
                {
                  "title": "Template Spring",
                  "description": "Estudiar seguridad",
                  "category": "WORK",
                  "difficulty": "HARD",
                  "taskType": "DAILY",
                  "baseXp": 120,
                  "mandatory": true,
                  "streakEligible": true,
                  "repeatable": false,
                  "maxCompletionsPerDay": 1,
                  "diminishingReturnsEnabled": false,
                  "dueTime": "20:00:00",
                  "weeklyClosingDay": null,
                  "publicTemplate": true,
                  "active": true,
                  "traitCodes": []
                }
                """);

        mockMvc.perform(get("/me/task-templates")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)]".formatted(templateId)).exists());

        mockMvc.perform(get("/me/task-templates/{templateId}", templateId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(templateId))
                .andExpect(jsonPath("$.title").value("Template Spring"))
                .andExpect(jsonPath("$.publicTemplate").value(true));

        MvcResult publicTemplatesResult = mockMvc.perform(get("/task-templates/public")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode publicTemplatesJson = objectMapper.readTree(publicTemplatesResult.getResponse().getContentAsString());
        List<JsonNode> templates = new ArrayList<>();
        publicTemplatesJson.forEach(templates::add);

        assertThat(templates)
                .anySatisfy(node -> {
                    assertThat(node.get("id").asLong()).isEqualTo(templateId);
                    assertThat(node.get("title").asText()).isEqualTo("Template Spring");
                    assertThat(node.get("publicTemplate").asBoolean()).isTrue();
                });

        mockMvc.perform(get("/task-templates/public/{templateId}", templateId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(templateId))
                .andExpect(jsonPath("$.title").value("Template Spring"));

        MvcResult createTaskFromTemplateResult = mockMvc.perform(post("/me/tasks/from-template")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "templateId": %d,
                                  "titleOverride": "Task desde template",
                                  "descriptionOverride": "Override description",
                                  "dueTimeOverride": "21:00:00",
                                  "weeklyClosingDayOverride": 5
                                }
                                """.formatted(templateId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.templateId").value(templateId))
                .andExpect(jsonPath("$.title").value("Task desde template"))
                .andExpect(jsonPath("$.description").value("Override description"))
                .andExpect(jsonPath("$.dueTime").value("21:00:00"))
                .andExpect(jsonPath("$.weeklyClosingDay").value(5))
                .andReturn();

        JsonNode createdTaskJson = objectMapper.readTree(createTaskFromTemplateResult.getResponse().getContentAsString());
        long taskId = createdTaskJson.get("id").asLong();

        Optional<User> userOpt = userRepository.findByUsername(auth.username());
        assertThat(userOpt).isPresent();

        Long userId = userOpt.get().getId();

        Optional<TaskTemplate> templateOpt = taskTemplateRepository.findByIdAndCreatorUserIdAndActiveTrue(templateId, userId);
        assertThat(templateOpt).isPresent();
        assertThat(templateOpt.get().isPublicTemplate()).isTrue();
        assertThat(templateOpt.get().getDueTime()).isEqualTo(LocalTime.of(20, 0));

        Optional<Task> taskOpt = taskRepository.findWithTraitsByIdAndUserId(taskId, userId);
        assertThat(taskOpt).isPresent();
        assertThat(taskOpt.get().getTemplate()).isNotNull();
        assertThat(taskOpt.get().getTemplate().getId()).isEqualTo(templateId);
        assertThat(taskOpt.get().getTitle()).isEqualTo("Task desde template");
        assertThat(taskOpt.get().getDescription()).isEqualTo("Override description");
        assertThat(taskOpt.get().getDueTime()).isEqualTo(LocalTime.of(21, 0));
        assertThat(taskOpt.get().getWeeklyClosingDay()).isEqualTo(5);
    }

    @Test
    @DisplayName("Una plantilla privada no debe ser visible en el listado público ni accesible por otro usuario")
    void privateTemplate_shouldNotBeVisiblePubliclyOrAccessibleToAnotherUser() throws Exception {
        AuthContext owner = registerAndGetAuthContext();
        AuthContext anotherUser = registerAndGetAuthContext();

        long privateTemplateId = createTemplate(owner.token(), """
                {
                  "title": "Private Template",
                  "description": "Solo del owner",
                  "category": "WORK",
                  "difficulty": "MEDIUM",
                  "taskType": "DAILY",
                  "baseXp": 80,
                  "mandatory": false,
                  "streakEligible": true,
                  "repeatable": false,
                  "maxCompletionsPerDay": 1,
                  "diminishingReturnsEnabled": false,
                  "dueTime": null,
                  "weeklyClosingDay": null,
                  "publicTemplate": false,
                  "active": true,
                  "traitCodes": []
                }
                """);

        MvcResult publicTemplatesResult = mockMvc.perform(get("/task-templates/public")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode publicTemplatesJson = objectMapper.readTree(publicTemplatesResult.getResponse().getContentAsString());
        List<JsonNode> templates = new ArrayList<>();
        publicTemplatesJson.forEach(templates::add);

        assertThat(templates)
                .noneSatisfy(node -> assertThat(node.get("id").asLong()).isEqualTo(privateTemplateId));

        mockMvc.perform(get("/task-templates/public/{templateId}", privateTemplateId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/me/tasks/from-template")
                        .header(HttpHeaders.AUTHORIZATION, bearer(anotherUser.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "templateId": %d
                                }
                                """.formatted(privateTemplateId)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/me/task-templates/{templateId}", privateTemplateId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(anotherUser.token())))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/me/task-templates/{templateId}", privateTemplateId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(privateTemplateId))
                .andExpect(jsonPath("$.publicTemplate").value(false));
    }

    @Test
    @DisplayName("No debe permitir crear tarea desde plantilla con weeklyClosingDayOverride inválido")
    void createTaskFromTemplate_shouldFailWhenWeeklyClosingDayOverrideInvalid() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        long templateId = createTemplate(auth.token(), """
                {
                  "title": "Weekly Template",
                  "description": "Template semanal",
                  "category": "WORK",
                  "difficulty": "MEDIUM",
                  "taskType": "DAILY",
                  "baseXp": 70,
                  "mandatory": false,
                  "streakEligible": true,
                  "repeatable": false,
                  "maxCompletionsPerDay": 1,
                  "diminishingReturnsEnabled": false,
                  "dueTime": null,
                  "weeklyClosingDay": 7,
                  "publicTemplate": true,
                  "active": true,
                  "traitCodes": []
                }
                """);

        MvcResult result = mockMvc.perform(post("/me/tasks/from-template")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "templateId": %d,
                                  "weeklyClosingDayOverride": 9
                                }
                                """.formatted(templateId)))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    private long createTemplate(String token, String requestBody) throws Exception {
        MvcResult result = mockMvc.perform(post("/me/task-templates")
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
                        .header(HttpHeaders.USER_AGENT, "JUnit-TaskTemplateFlowIT")
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