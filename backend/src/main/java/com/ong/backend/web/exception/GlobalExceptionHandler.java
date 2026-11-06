package com.ong.backend.web.exception;

import com.ong.backend.domain.exception.DocumentoArquivadoException;
import com.ong.backend.domain.exception.DocumentoNaoEncontradoException;
import com.ong.backend.domain.exception.TipoDocumentoInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TipoDocumentoInvalidoException.class)
    public ProblemDetail handleTipoDocumentoInvalido(TipoDocumentoInvalidoException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Tipo de Documento Inválido");
        problem.setType(URI.create("urn:problem-type:tipo-documento-invalido"));
        enrichProblemDetail(problem);
        return problem;
    }

    @ExceptionHandler(DocumentoArquivadoException.class)
    public ProblemDetail handleDocumentoArquivado(DocumentoArquivadoException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Operação Bloqueada - Documento Arquivado");
        problem.setType(URI.create("urn:problem-type:documento-arquivado"));
        enrichProblemDetail(problem);
        return problem;
    }

    @ExceptionHandler(DocumentoNaoEncontradoException.class)
    public ProblemDetail handleDocumentoNaoEncontrado(DocumentoNaoEncontradoException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Documento Não Encontrado");
        problem.setType(URI.create("urn:problem-type:documento-nao-encontrado"));
        enrichProblemDetail(problem);
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Falha de validação nos dados de entrada.");
        problem.setTitle("Erro de Validação");
        problem.setType(URI.create("urn:problem-type:validacao-campos"));

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("fieldErrors", fieldErrors);
        enrichProblemDetail(problem);
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Parâmetro Inválido");
        problem.setType(URI.create("urn:problem-type:parametro-invalido"));
        enrichProblemDetail(problem);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception ex) {
        String correlationId = UUID.randomUUID().toString();
        log.error("Erro inesperado no servidor. CorrelationId: {}", correlationId, ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado no servidor. Contate o suporte com o correlationId."
        );
        problem.setTitle("Erro Interno do Servidor");
        problem.setType(URI.create("urn:problem-type:internal-server-error"));
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("correlationId", correlationId);
        return problem;
    }

    private void enrichProblemDetail(ProblemDetail problem) {
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("correlationId", UUID.randomUUID().toString());
    }
}
