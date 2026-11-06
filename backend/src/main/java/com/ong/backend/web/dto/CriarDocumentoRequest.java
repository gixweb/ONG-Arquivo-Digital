package com.ong.backend.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CriarDocumentoRequest(
        @NotBlank(message = "O título do documento é obrigatório.")
        @Size(max = 255, message = "O título deve ter no máximo 255 caracteres.")
        String titulo,

        @NotBlank(message = "O tipo do documento é obrigatório (ata, contrato, relatorio, oficio).")
        String tipo,

        @NotNull(message = "A data do documento é obrigatória.")
        @JsonProperty("data_documento")
        LocalDate dataDocumento,

        @NotBlank(message = "O responsável é obrigatório.")
        @Size(max = 150, message = "O responsável deve ter no máximo 150 caracteres.")
        String responsavel,

        @JsonProperty("observacao_inicial")
        @Size(max = 500, message = "A observação inicial deve ter no máximo 500 caracteres.")
        String observacaoInicial
) {}
