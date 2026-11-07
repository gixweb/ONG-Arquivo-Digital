package com.ong.backend.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Anônimo tentando acessar API deve receber HTTP 401 Unauthorized (RFC 7807)")
    void anonymous_accessToApi_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/documentos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.title", is("Não Autenticado")));
    }

    @Test
    @DisplayName("Anônimo pode acessar arquivos estáticos do frontend livremente")
    void anonymous_canAccessStaticFrontend_returns200() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Perfil AUDITOR autenticado pode consultar documentos (GET 200 OK)")
    void auditor_canGetDocuments_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/documentos")
                        .with(httpBasic("auditor", "auditor123")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Perfil SECRETARIA autenticado pode consultar documentos (GET 200 OK)")
    void secretaria_canGetDocuments_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/documentos")
                        .with(httpBasic("secretaria", "secretaria123")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Perfil AUDITOR tentando cadastrar documento deve receber HTTP 403 Forbidden (RBAC)")
    void auditor_isForbiddenToCreateDocument_returns403() throws Exception {
        String payload = """
            {
                "titulo": "Tentativa por Auditor",
                "tipo": "ata",
                "data_documento": "2026-03-01",
                "responsavel": "Auditor Externo"
            }
        """;

        mockMvc.perform(post("/api/v1/documentos")
                        .with(httpBasic("auditor", "auditor123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Acesso Proibido")))
                .andExpect(jsonPath("$.detail", containsString("SECRETARIA")));
    }

    @Test
    @DisplayName("Perfil AUDITOR tentando arquivar documento deve receber HTTP 403 Forbidden (RBAC)")
    void auditor_isForbiddenToArchiveDocument_returns403() throws Exception {
        mockMvc.perform(patch("/api/v1/documentos/1/arquivar")
                        .with(httpBasic("auditor", "auditor123")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Acesso Proibido")));
    }

    @Test
    @DisplayName("Perfil SECRETARIA autenticado pode cadastrar documento com sucesso (POST 201 Created)")
    void secretaria_canCreateDocument_returns201() throws Exception {
        String payload = """
            {
                "titulo": "Ata de Posse da Secretaria",
                "tipo": "ata",
                "data_documento": "2026-01-15",
                "responsavel": "Secretária Geral",
                "observacao_inicial": "Registro formal"
            }
        """;

        mockMvc.perform(post("/api/v1/documentos")
                        .with(httpBasic("secretaria", "secretaria123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.titulo", is("Ata de Posse da Secretaria")))
                .andExpect(jsonPath("$.versao_atual", is(1)))
                .andExpect(jsonPath("$.status", is("vigente")));
    }
}
