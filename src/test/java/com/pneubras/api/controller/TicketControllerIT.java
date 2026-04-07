package com.pneubras.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TicketControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    private String login(String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", email, "password", password));
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String adminToken() throws Exception {
        return login(adminEmail, adminPassword);
    }

    private void registerUser(String email, String password, String role) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "Integration User",
                    "email", email,
                    "password", password,
                    "role", role
                ))))
            .andExpect(status().isOk());
    }

    private long createTicket(String bearerToken, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/tickets")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "title", title,
                    "description", "Descrição do chamado com tamanho suficiente.",
                    "priority", "MEDIUM"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ABERTO"))
            .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.get("id").asLong();
    }

    @Test
    @DisplayName("GET /tickets — 403 sem JWT (recurso protegido)")
    void listTickets_withoutAuth_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/tickets"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /tickets — 200 com JWT (lista paginada)")
    void listTickets_withAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/tickets")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken())
                .queryParam("page", "0")
                .queryParam("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /tickets?status=OPEN — filtro opcional")
    void listTickets_withStatusQuery_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/tickets")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken())
                .queryParam("status", "OPEN")
                .queryParam("page", "0")
                .queryParam("size", "10"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /tickets — cria ticket e GET por id como criador")
    void createAndGetById_asOwner() throws Exception {
        String email = "owner-" + UUID.randomUUID() + "@test.local";
        registerUser(email, "Password123", "USER");
        String token = login(email, "Password123");

        long id = createTicket(token, "Meu chamado IT");

        mockMvc.perform(get("/api/v1/tickets/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.title").value("Meu chamado IT"))
            .andExpect(jsonPath("$.createdBy.email").value(email));
    }

    @Test
    @DisplayName("GET /tickets/{id} — 403 quando outro USER tenta acessar ticket alheio")
    void getById_asOtherUser_returns403() throws Exception {
        String emailA = "user-a-" + UUID.randomUUID() + "@test.local";
        String emailB = "user-b-" + UUID.randomUUID() + "@test.local";
        registerUser(emailA, "Password123", "USER");
        registerUser(emailB, "Password123", "USER");
        String tokenA = login(emailA, "Password123");
        String tokenB = login(emailB, "Password123");

        long id = createTicket(tokenA, "Ticket privado");

        mockMvc.perform(get("/api/v1/tickets/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /tickets/{id} — 200 quando ADMIN acessa ticket de outro usuário")
    void getById_asAdmin_canAccessAnyTicket() throws Exception {
        String email = "creator-" + UUID.randomUUID() + "@test.local";
        registerUser(email, "Password123", "USER");
        String userToken = login(email, "Password123");
        long id = createTicket(userToken, "Ticket do usuário");

        mockMvc.perform(get("/api/v1/tickets/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @DisplayName("PATCH /tickets/{id} — atualização pelo criador")
    void patchTicket_asOwner_returns200() throws Exception {
        String email = "patch-" + UUID.randomUUID() + "@test.local";
        registerUser(email, "Password123", "USER");
        String token = login(email, "Password123");
        long id = createTicket(token, "Título original");

        mockMvc.perform(patch("/api/v1/tickets/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "title", "Título novo após patch",
                    "description", "Nova descrição com texto suficiente aqui."
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Título novo após patch"));
    }

    @Test
    @DisplayName("PATCH /tickets/{id}/status — 403 para USER")
    void patchStatus_asUser_returns403() throws Exception {
        String email = "u-" + UUID.randomUUID() + "@test.local";
        registerUser(email, "Password123", "USER");
        String token = login(email, "Password123");
        long id = createTicket(token, "Status test");

        mockMvc.perform(patch("/api/v1/tickets/" + id + "/status")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"IN_PROGRESS\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /tickets/{id}/status — 200 para AGENT (OPEN → IN_PROGRESS)")
    void patchStatus_asAgent_returns200() throws Exception {
        String userEmail = "creator-" + UUID.randomUUID() + "@test.local";
        String agentEmail = "agent-" + UUID.randomUUID() + "@test.local";
        registerUser(userEmail, "Password123", "USER");
        registerUser(agentEmail, "Password123", "AGENT");
        String userToken = login(userEmail, "Password123");
        String agentToken = login(agentEmail, "Password123");
        long id = createTicket(userToken, "Para agente atuar");

        mockMvc.perform(patch("/api/v1/tickets/" + id + "/status")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"IN_PROGRESS\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("EM_PROGRESSO"));
    }

    @Test
    @DisplayName("DELETE /tickets/{id} — 403 para USER")
    void delete_asUser_returns403() throws Exception {
        String email = "del-user-" + UUID.randomUUID() + "@test.local";
        registerUser(email, "Password123", "USER");
        String token = login(email, "Password123");
        long id = createTicket(token, "Delete forbid");

        mockMvc.perform(delete("/api/v1/tickets/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /tickets/{id} — 204 para ADMIN")
    void delete_asAdmin_returns204() throws Exception {
        String email = "del-subj-" + UUID.randomUUID() + "@test.local";
        registerUser(email, "Password123", "USER");
        String token = login(email, "Password123");
        long id = createTicket(token, "Para admin remover");

        mockMvc.perform(delete("/api/v1/tickets/" + id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /tickets — 400 quando validação falha")
    void createTicket_invalidBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"ab\",\"description\":\"ok\",\"priority\":\"MEDIUM\"}"))
            .andExpect(status().isBadRequest());
    }
}
