package com.ong.backend.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ong.backend.application.port.in.*;
import com.ong.backend.domain.exception.DocumentoArquivadoException;
import com.ong.backend.domain.exception.DocumentoNaoEncontradoException;
import com.ong.backend.domain.exception.TipoDocumentoInvalidoException;
import com.ong.backend.domain.model.Documento;
import com.ong.backend.domain.model.PaginatedResult;
import com.ong.backend.domain.model.TipoDocumento;
import com.ong.backend.domain.model.VersaoDocumento;
import com.ong.backend.web.dto.CriarDocumentoRequest;
import com.ong.backend.web.dto.NovaVersaoRequest;
import com.ong.backend.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DocumentoControllerSliceTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CriarDocumentoUseCase criarDocumentoUseCase;

    @Mock
    private BuscarDocumentoPorIdUseCase buscarDocumentoPorIdUseCase;

    @Mock
    private RegistrarNovaVersaoUseCase registrarNovaVersaoUseCase;

    @Mock
    private ArquivarDocumentoUseCase arquivarDocumentoUseCase;

    @Mock
    private ConsultarHistoricoVersoesUseCase consultarHistoricoVersoesUseCase;

    @Mock
    private BuscarDocumentosComFiltroUseCase buscarDocumentosComFiltroUseCase;

    @InjectMocks
    private DocumentoController controller;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/documentos - Should create document and return 201 Created with Location")
    void should_createDocument_return201() throws Exception {
        CriarDocumentoRequest request = new CriarDocumentoRequest(
                "Ata de Reunião Ordinária",
                "ata",
                LocalDate.of(2026, 3, 20),
                "Secretaria Executiva",
                "Versão inicial"
        );

        Documento docCriado = Documento.criar(
                "Ata de Reunião Ordinária",
                TipoDocumento.ATA,
                LocalDate.of(2026, 3, 20),
                "Secretaria Executiva",
                "Versão inicial"
        );
        java.lang.reflect.Field idField = Documento.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(docCriado, 10L);

        when(criarDocumentoUseCase.criar(any(CriarDocumentoCommand.class))).thenReturn(docCriado);

        mockMvc.perform(post("/api/v1/documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/documentos/10")))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.titulo", is("Ata de Reunião Ordinária")))
                .andExpect(jsonPath("$.tipo", is("ata")))
                .andExpect(jsonPath("$.versao_atual", is(1)))
                .andExpect(jsonPath("$.status", is("vigente")));
    }

    @Test
    @DisplayName("POST /api/v1/documentos - Should return 400 Bad Request when tipo is memorando (CA1)")
    void should_return400_when_tipoIsMemorando_CA1() throws Exception {
        CriarDocumentoRequest request = new CriarDocumentoRequest(
                "Memorando Interno 01",
                "memorando",
                LocalDate.of(2026, 3, 20),
                "Secretaria Executiva",
                null
        );

        when(criarDocumentoUseCase.criar(any(CriarDocumentoCommand.class)))
                .thenThrow(new TipoDocumentoInvalidoException("memorando"));

        mockMvc.perform(post("/api/v1/documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Tipo de Documento Inválido")))
                .andExpect(jsonPath("$.detail", containsString("Tipo de documento inválido: 'memorando'")))
                .andExpect(jsonPath("$.correlationId", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/documentos - Should return 400 Bad Request when mandatory fields are missing")
    void should_return400_when_fieldsMissing() throws Exception {
        CriarDocumentoRequest requestInvalido = new CriarDocumentoRequest(
                "",
                "",
                null,
                "",
                null
        );

        mockMvc.perform(post("/api/v1/documentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Erro de Validação")))
                .andExpect(jsonPath("$.fieldErrors.titulo", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.tipo", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.dataDocumento", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.responsavel", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/v1/documentos/{id} - Should return 200 OK with detailed document and history")
    void should_return200_when_foundById() throws Exception {
        Documento doc = Documento.criar("Contrato de Locação", TipoDocumento.CONTRATO, LocalDate.now(), "Diretoria", "Primeira versão");
        java.lang.reflect.Field idField = Documento.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(doc, 5L);

        when(buscarDocumentoPorIdUseCase.buscarPorId(5L)).thenReturn(doc);

        mockMvc.perform(get("/api/v1/documentos/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.titulo", is("Contrato de Locação")))
                .andExpect(jsonPath("$.tipo", is("contrato")))
                .andExpect(jsonPath("$.versao_atual", is(1)))
                .andExpect(jsonPath("$.versoes", hasSize(1)))
                .andExpect(jsonPath("$.versoes[0].numero", is(1)))
                .andExpect(jsonPath("$.versoes[0].observacao", is("Primeira versão")));
    }

    @Test
    @DisplayName("GET /api/v1/documentos/{id} - Should return 404 Not Found ProblemDetail when id does not exist")
    void should_return404_when_notFound() throws Exception {
        when(buscarDocumentoPorIdUseCase.buscarPorId(999L))
                .thenThrow(new DocumentoNaoEncontradoException(999L));

        mockMvc.perform(get("/api/v1/documentos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Documento Não Encontrado")))
                .andExpect(jsonPath("$.detail", containsString("999")))
                .andExpect(jsonPath("$.correlationId", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/documentos/{id}/versoes - Should register new version and return 201 Created (CA2)")
    void should_registerNewVersion_return201_CA2() throws Exception {
        NovaVersaoRequest request = new NovaVersaoRequest("Inclusão de cláusula aditiva");
        VersaoDocumento versao2 = new VersaoDocumento(2L, 7L, 2, "Inclusão de cláusula aditiva", null, Instant.now());

        when(registrarNovaVersaoUseCase.registrarNovaVersao(eq(7L), eq("Inclusão de cláusula aditiva")))
                .thenReturn(versao2);

        mockMvc.perform(post("/api/v1/documentos/7/versoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.numero", is(2)))
                .andExpect(jsonPath("$.observacao", is("Inclusão de cláusula aditiva")));
    }

    @Test
    @DisplayName("POST /api/v1/documentos/{id}/versoes - Should return 409 Conflict when document is archived (CA3)")
    void should_return409_when_documentArchived_CA3() throws Exception {
        NovaVersaoRequest request = new NovaVersaoRequest("Tentativa em documento arquivado");

        when(registrarNovaVersaoUseCase.registrarNovaVersao(eq(8L), any()))
                .thenThrow(new DocumentoArquivadoException(8L));

        mockMvc.perform(post("/api/v1/documentos/8/versoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Operação Bloqueada - Documento Arquivado")))
                .andExpect(jsonPath("$.detail", containsString("8")))
                .andExpect(jsonPath("$.detail", containsString("arquivado e não pode receber novas versões")))
                .andExpect(jsonPath("$.correlationId", notNullValue()));
    }

    @Test
    @DisplayName("PATCH /api/v1/documentos/{id}/arquivar - Should archive document and return 200 OK")
    void should_archiveDocument_return200() throws Exception {
        Documento doc = Documento.criar("Ata Reunião", TipoDocumento.ATA, LocalDate.now(), "Diretoria", null);
        doc.arquivar();
        java.lang.reflect.Field idField = Documento.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(doc, 3L);

        when(arquivarDocumentoUseCase.arquivar(3L)).thenReturn(doc);

        mockMvc.perform(patch("/api/v1/documentos/3/arquivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.status", is("arquivado")));
    }

    @Test
    @DisplayName("GET /api/v1/documentos/{id}/versoes - Should return list of versions ordered")
    void should_returnVersionHistory() throws Exception {
        VersaoDocumento v1 = new VersaoDocumento(1L, 3L, 1, "Versão inicial", null, Instant.now());
        VersaoDocumento v2 = new VersaoDocumento(2L, 3L, 2, "Segunda versão", null, Instant.now());

        when(consultarHistoricoVersoesUseCase.consultarHistorico(3L)).thenReturn(List.of(v1, v2));

        mockMvc.perform(get("/api/v1/documentos/3/versoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].numero", is(1)))
                .andExpect(jsonPath("$[1].numero", is(2)));
    }

    @Test
    @DisplayName("GET /api/v1/documentos - Should list documents with filters and pagination")
    void should_listDocumentsWithFilters() throws Exception {
        Documento doc = Documento.criar("Ata 01", TipoDocumento.ATA, LocalDate.of(2026, 2, 10), "Secretaria", null);
        java.lang.reflect.Field idField = Documento.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(doc, 1L);

        PaginatedResult<Documento> paginatedResult = new PaginatedResult<>(List.of(doc), 0, 10, 1, 1);
        when(buscarDocumentosComFiltroUseCase.buscarComFiltros(any(BuscarDocumentosFiltroQuery.class)))
                .thenReturn(paginatedResult);

        mockMvc.perform(get("/api/v1/documentos")
                        .param("tipo", "ata")
                        .param("data_inicio", "2026-01-01")
                        .param("data_fim", "2026-06-30")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id", is(1)))
                .andExpect(jsonPath("$.items[0].titulo", is("Ata 01")))
                .andExpect(jsonPath("$.items[0].tipo", is("ata")))
                .andExpect(jsonPath("$.total_elements", is(1)))
                .andExpect(jsonPath("$.total_pages", is(1)));
    }
}
