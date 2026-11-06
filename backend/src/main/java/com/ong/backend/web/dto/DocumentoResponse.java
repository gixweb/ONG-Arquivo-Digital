package com.ong.backend.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ong.backend.domain.model.Documento;

import java.time.Instant;
import java.time.LocalDate;

public record DocumentoResponse(
        Long id,
        String titulo,
        String tipo,
        @JsonProperty("data_documento")
        LocalDate dataDocumento,
        String responsavel,
        String status,
        @JsonProperty("versao_atual")
        int versaoAtual,
        @JsonProperty("criado_em")
        Instant criadoEm,
        @JsonProperty("atualizado_em")
        Instant atualizadoEm
) {
    public static DocumentoResponse fromDomain(Documento doc) {
        if (doc == null) return null;
        return new DocumentoResponse(
                doc.getId(),
                doc.getTitulo(),
                doc.getTipo().getCodigo(),
                doc.getDataDocumento(),
                doc.getResponsavel(),
                doc.getStatus().getCodigo(),
                doc.getVersaoAtual(),
                doc.getCriadoEm(),
                doc.getAtualizadoEm()
        );
    }
}
