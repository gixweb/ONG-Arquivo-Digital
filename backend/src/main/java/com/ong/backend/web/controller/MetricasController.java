package com.ong.backend.web.controller;

import com.ong.backend.application.port.in.ObterMetricasArquivoUseCase;
import com.ong.backend.web.dto.MetricasResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metricas")
@CrossOrigin(origins = "*")
public class MetricasController {

    private final ObterMetricasArquivoUseCase obterMetricasUseCase;

    public MetricasController(ObterMetricasArquivoUseCase obterMetricasUseCase) {
        this.obterMetricasUseCase = obterMetricasUseCase;
    }

    @GetMapping
    public ResponseEntity<MetricasResponse> obterMetricas() {
        return ResponseEntity.ok(MetricasResponse.fromDomain(obterMetricasUseCase.obterMetricas()));
    }
}
