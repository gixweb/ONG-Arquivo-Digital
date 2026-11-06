package com.ong.backend.web.controller;

import com.ong.backend.application.port.in.ObterMetricasArquivoUseCase;
import com.ong.backend.domain.model.MetricasArquivo;
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

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MetricasControllerSliceTest {

    private MockMvc mockMvc;

    @Mock
    private ObterMetricasArquivoUseCase obterMetricasUseCase;

    @InjectMocks
    private MetricasController metricasController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(metricasController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/metricas should return 200 with totals and por_tipo map")
    void should_getMetrics_return200() throws Exception {
        Map<String, Long> porTipo = new LinkedHashMap<>();
        porTipo.put("ata", 5L);
        porTipo.put("contrato", 3L);
        porTipo.put("relatorio", 2L);
        porTipo.put("oficio", 1L);

        MetricasArquivo metricas = new MetricasArquivo(11L, 9L, 2L, porTipo);
        when(obterMetricasUseCase.obterMetricas()).thenReturn(metricas);

        mockMvc.perform(get("/api/v1/metricas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(11)))
                .andExpect(jsonPath("$.vigentes", is(9)))
                .andExpect(jsonPath("$.arquivados", is(2)))
                .andExpect(jsonPath("$.por_tipo.ata", is(5)))
                .andExpect(jsonPath("$.por_tipo.contrato", is(3)))
                .andExpect(jsonPath("$.por_tipo.relatorio", is(2)))
                .andExpect(jsonPath("$.por_tipo.oficio", is(1)));

        verify(obterMetricasUseCase).obterMetricas();
    }
}
