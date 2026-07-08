package com.worksafe.backend.domain.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worksafe.backend.domain.auth.dto.request.ChangePasswordRequest;
import com.worksafe.backend.domain.auth.dto.request.LoginRequest;
import com.worksafe.backend.domain.auth.dto.request.RefreshTokenRequest;
import com.worksafe.backend.domain.auth.dto.request.SignupRequest;
import com.worksafe.backend.domain.auth.enums.UserRole;
import com.worksafe.backend.domain.auth.repository.RefreshTokenRepository;
import com.worksafe.backend.domain.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void signup_returns_user_without_tokens() throws Exception {
        String loginId = uniqueLoginId();

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupRequest(
                                loginId,
                                "Password123!",
                                "gildong",
                                UserRole.WORKER
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.loginId").value(loginId))
                .andExpect(jsonPath("$.data.name").value("gildong"))
                .andExpect(jsonPath("$.data.role").value("WORKER"))
                .andExpect(jsonPath("$.data.accessToken").doesNotExist())
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist());
    }

    @Test
    void login_returns_tokens_and_user() throws Exception {
        String loginId = uniqueLoginId();
        signup(loginId, "Password123!", "gildong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(
                                loginId,
                                "Password123!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.loginId").value(loginId))
                .andExpect(jsonPath("$.data.user.name").value("gildong"))
                .andExpect(jsonPath("$.data.tokenType").doesNotExist())
                .andExpect(jsonPath("$.data.expiresIn").doesNotExist());
    }

    @Test
    void refresh_rotates_refresh_token_and_returns_new_tokens() throws Exception {
        String loginId = uniqueLoginId();
        signup(loginId, "Password123!", "gildong");

        JsonNode loginData = login(loginId, "Password123!");
        String refreshToken = loginData.path("refreshToken").asText();

        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.loginId").value(loginId))
                .andReturn();

        String rotatedRefreshToken = objectMapper.readTree(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8)
        ).path("data").path("refreshToken").asText();
        assertThat(rotatedRefreshToken).isNotBlank();
        assertThat(rotatedRefreshToken).isNotEqualTo(refreshToken);
    }

    @Test
    void me_returns_current_user_when_access_token_is_valid() throws Exception {
        String loginId = uniqueLoginId();
        signup(loginId, "Password123!", "gildong");

        JsonNode loginData = login(loginId, "Password123!");
        String accessToken = loginData.path("accessToken").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loginId").value(loginId))
                .andExpect(jsonPath("$.data.name").value("gildong"))
                .andExpect(jsonPath("$.data.role").value("WORKER"));
    }

    @Test
    void change_password_updates_password_and_invalidates_old_credentials() throws Exception {
        String loginId = uniqueLoginId();
        signup(loginId, "Password123!", "gildong");

        JsonNode loginData = login(loginId, "Password123!");
        String accessToken = loginData.path("accessToken").asText();

        mockMvc.perform(patch("/api/auth/password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangePasswordRequest(
                                "Password123!",
                                "NewPassword123!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(
                                loginId,
                                "Password123!"
                        ))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(
                                loginId,
                                "NewPassword123!"
                        ))))
                .andExpect(status().isOk());
    }

    private void signup(String loginId, String password, String name) throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupRequest(
                                loginId,
                                password,
                                name,
                                UserRole.WORKER
                        ))))
                .andExpect(status().isCreated());
    }

    private JsonNode login(String loginId, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(loginId, password))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("data");
    }

    private String uniqueLoginId() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
