package com.ong.backend.infrastructure.persistence.adapter;

import com.ong.backend.application.port.out.DocumentoRepositoryPort;
import com.ong.backend.domain.model.Documento;
import com.ong.backend.domain.model.StatusDocumento;
import com.ong.backend.domain.model.TipoDocumento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DocumentoPersistenceAdapterIntegrationTest {

    @Autowired
    private DocumentoRepositoryPort repositoryPort;

    @Test
    @DisplayName("Should persist document with initial version via Flyway schema and retrieve it")
    void should_persistAndRetrieveDocument() {
        Documento novoDoc = Documento.criar(
                "Ata de Posse da Diretoria",
                TipoDocumento.ATA,
                LocalDate.of(2026, 1, 10),
                "Secretaria Geral",
                "Versão inicial lavrada em livro"
        );

        Documento salvo = repositoryPort.salvar(novoDoc);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getTitulo()).isEqualTo("Ata de Posse da Diretoria");
        assertThat(salvo.getTipo()).isEqualTo(TipoDocumento.ATA);
        assertThat(salvo.getVersaoAtual()).isEqualTo(1);
        assertThat(salvo.getStatus()).isEqualTo(StatusDocumento.VIGENTE);

        Optional<Documento> recuperado = repositoryPort.buscarPorIdComVersoes(salvo.getId());
        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getVersoes()).hasSize(1);
        assertThat(recuperado.get().getVersoes().get(0).getNumero()).isEqualTo(1);
        assertThat(recuperado.get().getVersoes().get(0).getObservacao()).isEqualTo("Versão inicial lavrada em livro");
    }

    @Test
    @DisplayName("Should persist new version and update versao_atual atomically (CA2)")
    void should_persistNewVersionAtomically_CA2() {
        Documento doc = Documento.criar(
                "Contrato Prestação de Serviços",
                TipoDocumento.CONTRATO,
                LocalDate.of(2026, 2, 1),
                "Jurídico",
                "Minuta inicial"
        );
        Documento salvo = repositoryPort.salvar(doc);

        salvo.registrarNovaVersao("Adicionada cláusula anticorrupção");
        Documento atualizado = repositoryPort.salvar(salvo);

        assertThat(atualizado.getVersaoAtual()).isEqualTo(2);

        Optional<Documento> comHistorico = repositoryPort.buscarPorIdComVersoes(atualizado.getId());
        assertThat(comHistorico).isPresent();
        assertThat(comHistorico.get().getVersoes()).hasSize(2);
        assertThat(comHistorico.get().getVersoes().get(1).getNumero()).isEqualTo(2);
        assertThat(comHistorico.get().getVersoes().get(1).getObservacao()).isEqualTo("Adicionada cláusula anticorrupção");
    }

    @Test
    @DisplayName("Should archive document and persist arquivado status")
    void should_archiveAndPersistStatus() {
        Documento doc = Documento.criar(
                "Relatório Semestral de Atividades",
                TipoDocumento.RELATORIO,
                LocalDate.of(2026, 6, 30),
                "Coordenação de Projetos",
                "Versão final"
        );
        Documento salvo = repositoryPort.salvar(doc);

        salvo.arquivar();
        repositoryPort.salvar(salvo);

        Optional<Documento> arquivado = repositoryPort.buscarPorId(salvo.getId());
        assertThat(arquivado).isPresent();
        assertThat(arquivado.get().getStatus()).isEqualTo(StatusDocumento.ARQUIVADO);
    }
}
