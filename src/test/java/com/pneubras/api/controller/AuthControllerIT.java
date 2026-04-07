package com.pneubras.api.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    private String obtainAdminToken() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "email", adminEmail,
            "password", adminPassword
        ));
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    @DisplayName("POST /login — 200 com token e dados do usuário quando credenciais válidas")
    void login_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "email", adminEmail,
            "password", adminPassword
        ));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.email").value(adminEmail))
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /login — 401 quando senha incorreta")
    void login_wrongPassword_returns401() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "email", adminEmail,
            "password", "WrongPassword999"
        ));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /login — 400 quando corpo inválido (validação)")
    void login_invalidBody_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "email", "not-an-email",
            "password", "short"
        ));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /register — 403 sem Bearer token")
    void register_withoutToken_returns403() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "name", "Novo Usuario",
            "email", "novo-" + UUID.randomUUID() + "@test.local",
            "password", "Password123",
            "role", "USER"
        ));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /register — 200 com token de admin e corpo válido")
    void register_withAdminToken_success() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@test.local";
        String body = objectMapper.writeValueAsString(Map.of(
            "name", "Novo Usuario",
            "email", email,
            "password", "Password123",
            "role", "USER"
        ));

        mockMvc.perform(post("/api/v1/auth/register")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + obtainAdminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.name").value("Novo Usuario"));
    }

    @Test
    @DisplayName("POST /register — 400 quando e-mail já existe")
    void register_duplicateEmail_returns400() throws Exception {
        String email = "dup-" + UUID.randomUUID() + "@test.local";
        Map<String, Object> payload = Map.of(
            "name", "Primeiro",
            "email", email,
            "password", "Password123",
            "role", "USER"
        );
        String json = objectMapper.writeValueAsString(payload);
        String auth = "Bearer " + obtainAdminToken();

        mockMvc.perform(post("/api/v1/auth/register")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/register")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Email already exists")));
    }

    @Test
    @DisplayName("POST /register — 400 quando validação Bean Validation falha")
    void register_validationFailure_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "name", "AB",
            "email", "valid-" + UUID.randomUUID() + "@test.local",
            "password", "Password123",
            "role", "USER"
        ));

        mockMvc.perform(post("/api/v1/auth/register")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + obtainAdminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }
}
