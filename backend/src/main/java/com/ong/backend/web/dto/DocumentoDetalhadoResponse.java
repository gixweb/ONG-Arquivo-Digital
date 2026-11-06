package com.ong.backend.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ong.backend.domain.model.Documento;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record DocumentoDetalhadoResponse(
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
        Instant atualizadoEm,
        List<VersaoResponse> versoes
) {
    public static DocumentoDetalhadoResponse fromDomain(Documento doc) {
        if (doc == null) return null;
        List<VersaoResponse> versoesResponse = doc.getVersoes() != null
                ? doc.getVersoes().stream().map(VersaoResponse::fromDomain).toList()
                : List.of();

        return new DocumentoDetalhadoResponse(
                doc.getId(),
                doc.getTitulo(),
                doc.getTipo().getCodigo(),
                doc.getDataDocumento(),
                doc.getResponsavel(),
                doc.getStatus().getCodigo(),
                doc.getVersaoAtual(),
                doc.getCriadoEm(),
                doc.getAtualizadoEm(),
                versoesResponse
        );
    }
}
