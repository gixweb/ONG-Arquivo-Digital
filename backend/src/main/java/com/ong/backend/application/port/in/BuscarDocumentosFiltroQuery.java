package com.ong.backend.application.port.in;

import java.time.LocalDate;

public record BuscarDocumentosFiltroQuery(
        String tipo,
        LocalDate dataInicio,
        LocalDate dataFim,
        String titulo,
        int page,
        int size
) {
    public BuscarDocumentosFiltroQuery {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("A data de início não pode ser posterior à data de término.");
        }
        page = Math.max(page, 0);
        size = size > 0 ? Math.min(size, 100) : 10;
    }
}
