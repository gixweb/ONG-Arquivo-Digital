package com.ong.backend.web.exception;

import com.ong.backend.domain.exception.DocumentoArquivadoException;
import com.ong.backend.domain.exception.DocumentoNaoEncontradoException;
import com.ong.backend.domain.exception.TipoDocumentoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle TipoDocumentoInvalidoException returning 400 ProblemDetail (CA1)")
    void should_return400_when_tipoDocumentoInvalido() {
        TipoDocumentoInvalidoException ex = new TipoDocumentoInvalidoException("memorando");
        ProblemDetail problem = handler.handleTipoDocumentoInvalido(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Tipo de Documento Inválido");
        assertThat(problem.getDetail()).contains("memorando");
        assertThat(problem.getProperties()).containsKey("correlationId");
        assertThat(problem.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Should handle DocumentoArquivadoException returning 409 Conflict (CA3)")
    void should_return409_when_documentoArquivado() {
        DocumentoArquivadoException ex = new DocumentoArquivadoException(42L);
        ProblemDetail problem = handler.handleDocumentoArquivado(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getTitle()).isEqualTo("Operação Bloqueada - Documento Arquivado");
        assertThat(problem.getDetail()).contains("42");
        assertThat(problem.getProperties()).containsKey("correlationId");
        assertThat(problem.getProperties()).containsKey("timestamp");
    }

    @Test
    @DisplayName("Should handle DocumentoNaoEncontradoException returning 404 Not Found")
    void should_return404_when_documentoNaoEncontrado() {
        DocumentoNaoEncontradoException ex = new DocumentoNaoEncontradoException(99L);
        ProblemDetail problem = handler.handleDocumentoNaoEncontrado(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getTitle()).isEqualTo("Documento Não Encontrado");
        assertThat(problem.getDetail()).contains("99");
        assertThat(problem.getProperties()).containsKey("correlationId");
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException returning 400 with fieldErrors")
    void should_return400_with_fieldErrors_when_validationFails() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("documentoDTO", "titulo", "O título é obrigatório");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ProblemDetail problem = handler.handleValidationErrors(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Erro de Validação");
        assertThat(problem.getProperties()).containsKey("fieldErrors");

        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors = (Map<String, String>) problem.getProperties().get("fieldErrors");
        assertThat(fieldErrors).containsEntry("titulo", "O título é obrigatório");
    }

    @Test
    @DisplayName("Should handle unexpected exception returning 500 with correlationId and no stack trace")
    void should_return500_with_correlationId_when_unexpectedError() {
        RuntimeException ex = new RuntimeException("Falha catastrófica de infraestrutura!");
        ProblemDetail problem = handler.handleUnexpectedException(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getTitle()).isEqualTo("Erro Interno do Servidor");
        assertThat(problem.getDetail()).doesNotContain("Falha catastrófica");
        assertThat(problem.getDetail()).contains("correlationId");
        assertThat(problem.getProperties()).containsKey("correlationId");
        assertThat(problem.getProperties()).containsKey("timestamp");
    }
}
