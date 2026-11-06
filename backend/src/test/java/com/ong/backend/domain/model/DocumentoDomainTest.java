package com.ong.backend.domain.model;

import com.ong.backend.domain.exception.DocumentoArquivadoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentoDomainTest {

    @Test
    @DisplayName("Should create document starting at version 1 with status vigente and 1 history entry")
    void should_createDocument_with_version1_and_vigenteStatus() {
        Documento doc = Documento.criar(
                "Ata da Assembleia Geral",
                TipoDocumento.ATA,
                LocalDate.of(2026, 3, 15),
                "Secretaria Executiva",
                "Abertura inicial"
        );

        assertThat(doc.getTitulo()).isEqualTo("Ata da Assembleia Geral");
        assertThat(doc.getTipo()).isEqualTo(TipoDocumento.ATA);
        assertThat(doc.getStatus()).isEqualTo(StatusDocumento.VIGENTE);
        assertThat(doc.getVersaoAtual()).isEqualTo(1);
        assertThat(doc.getVersoes()).hasSize(1);
        assertThat(doc.getVersoes().get(0).getNumero()).isEqualTo(1);
        assertThat(doc.getVersoes().get(0).getObservacao()).isEqualTo("Abertura inicial");
        assertThat(doc.getId()).isNull();
        assertThat(doc.getCriadoEm()).isNotNull();
        assertThat(doc.getAtualizadoEm()).isNotNull();
    }

    @Test
    @DisplayName("Should create document with default observation when observation is null or blank")
    void should_createDocument_with_defaultObservation_when_nullOrBlank() {
        Documento doc1 = Documento.criar("Ofício", TipoDocumento.OFICIO, LocalDate.now(), "Admin", null);
        assertThat(doc1.getVersoes().get(0).getObservacao()).isEqualTo("Criação inicial do documento.");

        Documento doc2 = Documento.criar("Ofício 2", TipoDocumento.OFICIO, LocalDate.now(), "Admin", "   ");
        assertThat(doc2.getVersoes().get(0).getObservacao()).isEqualTo("Criação inicial do documento.");
    }

    @Test
    @DisplayName("Should increment version to 2 and maintain 2 history rows (CA2)")
    void should_incrementVersionTo2_and_maintainHistory_CA2() {
        Documento doc = Documento.criar(
                "Contrato de Fornecimento",
                TipoDocumento.CONTRATO,
                LocalDate.of(2026, 4, 1),
                "Departamento Financeiro",
                null
        );

        VersaoDocumento novaVersao = doc.registrarNovaVersao("Adicionada cláusula de auditoria");

        assertThat(doc.getVersaoAtual()).isEqualTo(2);
        assertThat(doc.getVersoes()).hasSize(2);
        assertThat(novaVersao.getNumero()).isEqualTo(2);
        assertThat(novaVersao.getObservacao()).isEqualTo("Adicionada cláusula de auditoria");
    }

    @Test
    @DisplayName("Should archive document and block new versions with DocumentoArquivadoException (CA3)")
    void should_archiveDocument_and_blockNewVersion_CA3() {
        Documento doc = Documento.criar(
                "Relatório Financeiro Anual",
                TipoDocumento.RELATORIO,
                LocalDate.of(2026, 5, 10),
                "Tesouraria",
                "Versão final aprovada"
        );

        doc.arquivar();

        assertThat(doc.getStatus()).isEqualTo(StatusDocumento.ARQUIVADO);
        assertThatThrownBy(() -> doc.registrarNovaVersao("Tentativa de alteração"))
                .isInstanceOf(DocumentoArquivadoException.class)
                .hasMessageContaining("arquivado e não pode receber novas versões");
    }

    @Test
    @DisplayName("Should update metadata successfully")
    void should_updateMetadata_successfully() {
        Documento doc = Documento.criar("Título Original", TipoDocumento.OFICIO, LocalDate.of(2026, 1, 1), "Resp", null);
        LocalDate novaData = LocalDate.of(2026, 2, 2);

        doc.atualizarMetadados(" Novo Título ", novaData, " Novo Responsável ");

        assertThat(doc.getTitulo()).isEqualTo("Novo Título");
        assertThat(doc.getDataDocumento()).isEqualTo(novaData);
        assertThat(doc.getResponsavel()).isEqualTo("Novo Responsável");
    }

    @Test
    @DisplayName("Should reject updating metadata if document is archived")
    void should_rejectUpdatingMetadata_when_archived() {
        Documento doc = Documento.criar("Título", TipoDocumento.OFICIO, LocalDate.now(), "Resp", null);
        doc.arquivar();

        assertThatThrownBy(() -> doc.atualizarMetadados("Novo Título", LocalDate.now(), "Novo Resp"))
                .isInstanceOf(DocumentoArquivadoException.class);
    }

    @Test
    @DisplayName("Should reject blank fields on updating metadata")
    void should_rejectBlankFieldsOnUpdatingMetadata() {
        Documento doc = Documento.criar("Título", TipoDocumento.OFICIO, LocalDate.now(), "Resp", null);

        assertThatThrownBy(() -> doc.atualizarMetadados("", LocalDate.now(), "Resp"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> doc.atualizarMetadados("Título", null, "Resp"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> doc.atualizarMetadados("Título", LocalDate.now(), "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should support full constructor with fallback defaults")
    void should_supportFullConstructorWithDefaults() {
        Documento doc = new Documento(
                10L,
                "Título",
                TipoDocumento.ATA,
                LocalDate.now(),
                "Responsável",
                null,
                0,
                null,
                null,
                null
        );

        assertThat(doc.getId()).isEqualTo(10L);
        assertThat(doc.getStatus()).isEqualTo(StatusDocumento.VIGENTE);
        assertThat(doc.getVersaoAtual()).isEqualTo(1);
        assertThat(doc.getCriadoEm()).isNotNull();
        assertThat(doc.getAtualizadoEm()).isEqualTo(doc.getCriadoEm());
        assertThat(doc.getVersoes()).isEmpty();
    }

    @Test
    @DisplayName("Should reject blank title or null fields on creation")
    void should_rejectInvalidInputsOnCreation() {
        assertThatThrownBy(() -> Documento.criar("", TipoDocumento.ATA, LocalDate.now(), "Resp", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("título");

        assertThatThrownBy(() -> Documento.criar("Título", null, LocalDate.now(), "Resp", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tipo");

        assertThatThrownBy(() -> Documento.criar("Título", TipoDocumento.ATA, null, "Resp", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data");

        assertThatThrownBy(() -> Documento.criar("Título", TipoDocumento.ATA, LocalDate.now(), "  ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("responsável");
    }

    @Test
    @DisplayName("Should reject empty observation on new version")
    void should_rejectEmptyObservationOnNewVersion() {
        Documento doc = Documento.criar("Ofício 01", TipoDocumento.OFICIO, LocalDate.now(), "Diretoria", null);

        assertThatThrownBy(() -> doc.registrarNovaVersao("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("observação");
    }

    @Test
    @DisplayName("Should verify equals and hashCode based on ID")
    void should_verifyEqualsAndHashCode() {
        Documento d1 = new Documento(1L, "T1", TipoDocumento.ATA, LocalDate.now(), "R1", StatusDocumento.VIGENTE, 1, Instant.now(), Instant.now(), List.of());
        Documento d1Identical = new Documento(1L, "T2", TipoDocumento.OFICIO, LocalDate.now(), "R2", StatusDocumento.ARQUIVADO, 2, Instant.now(), Instant.now(), List.of());
        Documento d2 = new Documento(2L, "T1", TipoDocumento.ATA, LocalDate.now(), "R1", StatusDocumento.VIGENTE, 1, Instant.now(), Instant.now(), List.of());

        assertThat(d1.equals(d1)).isTrue();
        assertThat(d1.equals(d1Identical)).isTrue();
        assertThat(d1.hashCode()).isEqualTo(d1Identical.hashCode());

        assertThat(d1.equals(null)).isFalse();
        assertThat(d1.equals("String")).isFalse();
        assertThat(d1.equals(d2)).isFalse();
    }
}
