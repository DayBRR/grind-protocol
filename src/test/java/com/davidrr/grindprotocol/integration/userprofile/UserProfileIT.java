package com.davidrr.grindprotocol.integration.userprofile;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class UserProfileIT extends AbstractPostgresIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    @DisplayName("GET /me/profile debe devolver el perfil creado automáticamente tras register")
    void getMyProfile_shouldReturnAutoCreatedProfileAfterRegister() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        mockMvc.perform(get("/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(auth.username()))
                .andExpect(jsonPath("$.displayName").value(auth.username()))
                .andExpect(jsonPath("$.dailyTaskGoal").value(3))
                .andExpect(jsonPath("$.totalXp").value(0))
                .andExpect(jsonPath("$.corePoints").value(0))
                .andExpect(jsonPath("$.currentStreak").value(0))
                .andExpect(jsonPath("$.bestStreak").value(0))
                .andExpect(jsonPath("$.lastEvaluatedDate").doesNotExist());

        Optional<User> userOpt = userRepository.findByUsername(auth.username());
        assertThat(userOpt).isPresent();

        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(userOpt.get().getId());
        assertThat(profileOpt).isPresent();
        assertThat(profileOpt.get().getDisplayName()).isEqualTo(auth.username());
        assertThat(profileOpt.get().getDailyTaskGoal()).isEqualTo(3);
        assertThat(profileOpt.get().getTotalXp()).isEqualTo(0L);
        assertThat(profileOpt.get().getCorePoints()).isEqualTo(0L);
        assertThat(profileOpt.get().getCurrentStreak()).isEqualTo(0);
        assertThat(profileOpt.get().getBestStreak()).isEqualTo(0);
    }

    @Test
    @DisplayName("PUT /me/profile debe actualizar displayName y dailyTaskGoal")
    void updateMyProfile_shouldUpdateEditableFields() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        mockMvc.perform(put("/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "David Ruiz",
                                  "dailyTaskGoal": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(auth.username()))
                .andExpect(jsonPath("$.displayName").value("David Ruiz"))
                .andExpect(jsonPath("$.dailyTaskGoal").value(5))
                .andExpect(jsonPath("$.totalXp").value(0))
                .andExpect(jsonPath("$.corePoints").value(0))
                .andExpect(jsonPath("$.currentStreak").value(0))
                .andExpect(jsonPath("$.bestStreak").value(0));

        Optional<User> userOpt = userRepository.findByUsername(auth.username());
        assertThat(userOpt).isPresent();

        Optional<UserProfile> profileOpt = userProfileRepository.findByUserId(userOpt.get().getId());
        assertThat(profileOpt).isPresent();
        assertThat(profileOpt.get().getDisplayName()).isEqualTo("David Ruiz");
        assertThat(profileOpt.get().getDailyTaskGoal()).isEqualTo(5);
    }

    @Test
    @DisplayName("PUT /me/profile no debe modificar campos no editables del progreso")
    void updateMyProfile_shouldNotModifyProgressFields() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        Optional<User> userOpt = userRepository.findByUsername(auth.username());
        assertThat(userOpt).isPresent();

        User user = userOpt.get();

        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElseThrow();
        profile.setTotalXp(250L);
        profile.setCorePoints(40L);
        profile.setCurrentStreak(7);
        profile.setBestStreak(10);
        userProfileRepository.save(profile);

        mockMvc.perform(put("/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "Nuevo Nombre",
                                  "dailyTaskGoal": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Nuevo Nombre"))
                .andExpect(jsonPath("$.dailyTaskGoal").value(4))
                .andExpect(jsonPath("$.totalXp").value(250))
                .andExpect(jsonPath("$.corePoints").value(40))
                .andExpect(jsonPath("$.currentStreak").value(7))
                .andExpect(jsonPath("$.bestStreak").value(10));

        UserProfile updatedProfile = userProfileRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(updatedProfile.getDisplayName()).isEqualTo("Nuevo Nombre");
        assertThat(updatedProfile.getDailyTaskGoal()).isEqualTo(4);
        assertThat(updatedProfile.getTotalXp()).isEqualTo(250L);
        assertThat(updatedProfile.getCorePoints()).isEqualTo(40L);
        assertThat(updatedProfile.getCurrentStreak()).isEqualTo(7);
        assertThat(updatedProfile.getBestStreak()).isEqualTo(10);
    }

    @Test
    @DisplayName("GET /me/profile debe devolver 401 sin token")
    void getMyProfile_shouldReturnUnauthorized_whenNoToken() throws Exception {
        mockMvc.perform(get("/me/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /me/profile debe devolver 400 con datos inválidos")
    void updateMyProfile_shouldReturnBadRequest_whenRequestInvalid() throws Exception {
        AuthContext auth = registerAndGetAuthContext();

        mockMvc.perform(put("/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(auth.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "",
                                  "dailyTaskGoal": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
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
                        .header(HttpHeaders.USER_AGENT, "JUnit-UserProfileIT")
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