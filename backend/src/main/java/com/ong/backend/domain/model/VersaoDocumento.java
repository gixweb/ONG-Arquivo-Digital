package com.ong.backend.domain.model;

import java.time.Instant;
import java.util.Objects;

public class VersaoDocumento {

    private final Long id;
    private final Long documentoId;
    private final int numero;
    private final String observacao;
    private final String blobNome;
    private final Instant criadaEm;

    public VersaoDocumento(Long id, Long documentoId, int numero, String observacao, String blobNome, Instant criadaEm) {
        if (numero < 1) {
            throw new IllegalArgumentException("O número da versão deve ser maior ou igual a 1.");
        }
        if (observacao == null || observacao.isBlank()) {
            throw new IllegalArgumentException("A observação da versão é obrigatória.");
        }
        this.id = id;
        this.documentoId = documentoId;
        this.numero = numero;
        this.observacao = observacao.trim();
        this.blobNome = blobNome != null && !blobNome.isBlank() ? blobNome.trim() : null;
        this.criadaEm = criadaEm != null ? criadaEm : Instant.now();
    }

    public static VersaoDocumento criar(Long documentoId, int numero, String observacao) {
        return new VersaoDocumento(null, documentoId, numero, observacao, null, Instant.now());
    }

    public Long getId() {
        return id;
    }

    public Long getDocumentoId() {
        return documentoId;
    }

    public int getNumero() {
        return numero;
    }

    public String getObservacao() {
        return observacao;
    }

    public String getBlobNome() {
        return blobNome;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersaoDocumento that = (VersaoDocumento) o;
        return numero == that.numero && Objects.equals(documentoId, that.documentoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentoId, numero);
    }
}
