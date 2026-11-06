package com.ong.backend.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ong.backend.application.port.in.BuscarDocumentoPorIdUseCase;
import com.ong.backend.application.port.in.CriarDocumentoCommand;
import com.ong.backend.application.port.in.CriarDocumentoUseCase;
import com.ong.backend.domain.exception.DocumentoNaoEncontradoException;
import com.ong.backend.domain.exception.TipoDocumentoInvalidoException;
import com.ong.backend.domain.model.Documento;
import com.ong.backend.domain.model.TipoDocumento;
import com.ong.backend.web.dto.CriarDocumentoRequest;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DocumentoControllerSliceTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CriarDocumentoUseCase criarDocumentoUseCase;

    @Mock
    private BuscarDocumentoPorIdUseCase buscarDocumentoPorIdUseCase;

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
}
