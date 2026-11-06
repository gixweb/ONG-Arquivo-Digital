package com.ong.backend.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusDocumentoTest {

    @ParameterizedTest
    @ValueSource(strings = {"vigente", "VIGENTE", "arquivado", "ARQUIVADO"})
    @DisplayName("Should successfully parse valid statuses")
    void should_parseValidStatuses(String valor) {
        StatusDocumento status = StatusDocumento.from(valor);
        assertThat(status).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "cancelado", "pendente"})
    @DisplayName("Should throw IllegalArgumentException on invalid status")
    void should_throwException_when_statusIsInvalid(String valor) {
        assertThatThrownBy(() -> StatusDocumento.from(valor))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when status is null")
    void should_throwException_when_statusIsNull() {
        assertThatThrownBy(() -> StatusDocumento.from(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should return correct lowercase code")
    void should_returnCorrectCode() {
        assertThat(StatusDocumento.VIGENTE.getCodigo()).isEqualTo("vigente");
        assertThat(StatusDocumento.ARQUIVADO.getCodigo()).isEqualTo("arquivado");
    }
}
