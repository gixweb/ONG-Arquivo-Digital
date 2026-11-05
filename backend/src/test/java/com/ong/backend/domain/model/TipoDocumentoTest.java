package com.ong.backend.domain.model;

import com.ong.backend.domain.exception.TipoDocumentoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TipoDocumentoTest {

    @ParameterizedTest
    @ValueSource(strings = {"ata", "ATA", "  ata  ", "contrato", "CONTRATO", "relatorio", "oficio"})
    @DisplayName("Should successfully parse valid document types regardless of case or spaces")
    void should_parseValidDocumentTypes(String valor) {
        TipoDocumento tipo = TipoDocumento.from(valor);
        assertThat(tipo).isNotNull();
    }

    @Test
    @DisplayName("Should reject 'memorando' with TipoDocumentoInvalidoException (CA1)")
    void should_throwException_when_tipoIsMemorando_CA1() {
        assertThatThrownBy(() -> TipoDocumento.from("memorando"))
                .isInstanceOf(TipoDocumentoInvalidoException.class)
                .hasMessageContaining("Tipo de documento inválido: 'memorando'")
                .hasMessageContaining("ata, contrato, relatorio, oficio");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "certidao", "decreto"})
    @DisplayName("Should reject invalid or empty document types")
    void should_throwException_when_tipoIsInvalid(String valor) {
        assertThatThrownBy(() -> TipoDocumento.from(valor))
                .isInstanceOf(TipoDocumentoInvalidoException.class);
    }
}
