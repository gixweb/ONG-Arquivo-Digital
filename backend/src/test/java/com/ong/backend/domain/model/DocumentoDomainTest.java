package com.ong.backend.domain.model;

import com.ong.backend.domain.exception.DocumentoArquivadoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

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
}
