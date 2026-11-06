package com.ong.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NovaVersaoRequest(
        @NotBlank(message = "A observação do motivo da nova versão é obrigatória.")
        @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres.")
        String observacao
) {}
