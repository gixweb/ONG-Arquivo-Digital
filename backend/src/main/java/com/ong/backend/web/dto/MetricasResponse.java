package com.ong.backend.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ong.backend.domain.model.MetricasArquivo;

import java.util.Map;

public record MetricasResponse(
        long total,
        long vigentes,
        long arquivados,
        @JsonProperty("por_tipo")
        Map<String, Long> porTipo
) {
    public static MetricasResponse fromDomain(MetricasArquivo metricas) {
        if (metricas == null) {
            return new MetricasResponse(0, 0, 0, Map.of());
        }
        return new MetricasResponse(
                metricas.total(),
                metricas.vigentes(),
                metricas.arquivados(),
                metricas.porTipo()
        );
    }
}
