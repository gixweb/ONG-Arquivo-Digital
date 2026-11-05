package com.ong.backend.domain.model;

import com.ong.backend.domain.exception.TipoDocumentoInvalidoException;

import java.util.Arrays;

public enum TipoDocumento {
    ATA("ata"),
    CONTRATO("contrato"),
    RELATORIO("relatorio"),
    OFICIO("oficio");

    private final String codigo;

    TipoDocumento(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public static TipoDocumento from(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new TipoDocumentoInvalidoException("vazio ou nulo");
        }
        String normalizado = valor.trim().toLowerCase();
        return Arrays.stream(values())
                .filter(t -> t.codigo.equals(normalizado))
                .findFirst()
                .orElseThrow(() -> new TipoDocumentoInvalidoException(valor));
    }
}
