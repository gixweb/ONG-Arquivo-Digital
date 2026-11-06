package com.ong.backend.application.port.in;

import java.time.LocalDate;

public record CriarDocumentoCommand(
        String titulo,
        String tipo,
        LocalDate dataDocumento,
        String responsavel,
        String observacaoInicial
) {}
