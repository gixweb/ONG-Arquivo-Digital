package com.ong.backend.domain.model;

import java.util.Map;

public record MetricasArquivo(
        long total,
        long vigentes,
        long arquivados,
        Map<String, Long> porTipo
) {
}
