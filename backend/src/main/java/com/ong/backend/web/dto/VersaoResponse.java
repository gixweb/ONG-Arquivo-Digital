package com.ong.backend.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ong.backend.domain.model.VersaoDocumento;

import java.time.Instant;

public record VersaoResponse(
        Long id,
        int numero,
        String observacao,
        @JsonProperty("blob_nome")
        String blobNome,
        @JsonProperty("criada_em")
        Instant criadaEm
) {
    public static VersaoResponse fromDomain(VersaoDocumento versao) {
        if (versao == null) return null;
        return new VersaoResponse(
                versao.getId(),
                versao.getNumero(),
                versao.getObservacao(),
                versao.getBlobNome(),
                versao.getCriadaEm()
        );
    }
}
