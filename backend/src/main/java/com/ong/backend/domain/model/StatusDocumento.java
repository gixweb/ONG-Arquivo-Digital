package com.ong.backend.domain.model;

import java.util.Arrays;

public enum StatusDocumento {
    VIGENTE("vigente"),
    ARQUIVADO("arquivado");

    private final String codigo;

    StatusDocumento(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public static StatusDocumento from(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Status do documento não pode ser nulo ou vazio.");
        }
        String normalizado = valor.trim().toLowerCase();
        return Arrays.stream(values())
                .filter(s -> s.codigo.equals(normalizado))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Status desconhecido: " + valor));
    }
}
