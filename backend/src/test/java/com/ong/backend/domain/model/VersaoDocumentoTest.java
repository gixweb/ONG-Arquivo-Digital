package com.ong.backend.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VersaoDocumentoTest {

    @Test
    @DisplayName("Should create versao with valid data and default timestamps/blobs")
    void should_createVersao_with_validData() {
        Instant agora = Instant.now();
        VersaoDocumento versao = new VersaoDocumento(1L, 10L, 1, "Observação inicial", "blob123", agora);

        assertThat(versao.getId()).isEqualTo(1L);
        assertThat(versao.getDocumentoId()).isEqualTo(10L);
        assertThat(versao.getNumero()).isEqualTo(1);
        assertThat(versao.getObservacao()).isEqualTo("Observação inicial");
        assertThat(versao.getBlobNome()).isEqualTo("blob123");
        assertThat(versao.getCriadaEm()).isEqualTo(agora);
    }

    @Test
    @DisplayName("Should handle null or blank blobNome and null criadaEm")
    void should_handleNullOrBlankBlobNome_and_nullCriadaEm() {
        VersaoDocumento versao1 = new VersaoDocumento(null, 10L, 1, "Obs", null, null);
        assertThat(versao1.getBlobNome()).isNull();
        assertThat(versao1.getCriadaEm()).isNotNull();

        VersaoDocumento versao2 = new VersaoDocumento(null, 10L, 1, "Obs", "   ", null);
        assertThat(versao2.getBlobNome()).isNull();
    }

    @Test
    @DisplayName("Should create via static factory method criar")
    void should_createViaStaticFactory() {
        VersaoDocumento versao = VersaoDocumento.criar(5L, 2, "Segunda versao");

        assertThat(versao.getId()).isNull();
        assertThat(versao.getDocumentoId()).isEqualTo(5L);
        assertThat(versao.getNumero()).isEqualTo(2);
        assertThat(versao.getObservacao()).isEqualTo("Segunda versao");
        assertThat(versao.getBlobNome()).isNull();
        assertThat(versao.getCriadaEm()).isNotNull();
    }

    @Test
    @DisplayName("Should reject invalid numero (< 1)")
    void should_rejectInvalidNumero() {
        assertThatThrownBy(() -> new VersaoDocumento(null, 1L, 0, "Obs", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("número da versão deve ser maior ou igual a 1");

        assertThatThrownBy(() -> new VersaoDocumento(null, 1L, -5, "Obs", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject null or blank observacao")
    void should_rejectNullOrBlankObservacao() {
        assertThatThrownBy(() -> new VersaoDocumento(null, 1L, 1, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("observação da versão é obrigatória");

        assertThatThrownBy(() -> new VersaoDocumento(null, 1L, 1, "   ", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("observação da versão é obrigatória");
    }

    @Test
    @DisplayName("Should verify equals and hashCode contracts")
    void should_verifyEqualsAndHashCode() {
        VersaoDocumento v1 = new VersaoDocumento(1L, 10L, 1, "Obs", null, null);
        VersaoDocumento v1Identical = new VersaoDocumento(2L, 10L, 1, "Outra Obs", null, null);
        VersaoDocumento vDifferentNumero = new VersaoDocumento(1L, 10L, 2, "Obs", null, null);
        VersaoDocumento vDifferentDocId = new VersaoDocumento(1L, 20L, 1, "Obs", null, null);

        assertThat(v1.equals(v1)).isTrue();
        assertThat(v1.equals(v1Identical)).isTrue();
        assertThat(v1.hashCode()).isEqualTo(v1Identical.hashCode());

        assertThat(v1.equals(null)).isFalse();
        assertThat(v1.equals("uma string")).isFalse();
        assertThat(v1.equals(vDifferentNumero)).isFalse();
        assertThat(v1.equals(vDifferentDocId)).isFalse();
    }
}
