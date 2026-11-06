package com.ong.backend.infrastructure.persistence.adapter;

import com.ong.backend.application.port.out.DocumentoRepositoryPort;
import com.ong.backend.domain.model.Documento;
import com.ong.backend.domain.model.MetricasArquivo;
import com.ong.backend.domain.model.PaginatedResult;
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
                "Coordenação Financeira",
                "Versão inicial"
        );
        Documento salvo = repositoryPort.salvar(doc);

        salvo.registrarNovaVersao("Adicionada cláusula de rescisão no item 5");
        Documento atualizado = repositoryPort.salvar(salvo);

        assertThat(atualizado.getVersaoAtual()).isEqualTo(2);
        assertThat(atualizado.getVersoes()).hasSize(2);

        Optional<Documento> recuperado = repositoryPort.buscarPorIdComVersoes(salvo.getId());
        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getVersaoAtual()).isEqualTo(2);
        assertThat(recuperado.get().getVersoes()).hasSize(2);
        assertThat(recuperado.get().getVersoes().get(1).getNumero()).isEqualTo(2);
        assertThat(recuperado.get().getVersoes().get(1).getObservacao()).isEqualTo("Adicionada cláusula de rescisão no item 5");
    }

    @Test
    @DisplayName("Should archive document and persist status change (RF04)")
    void should_persistArchivedStatus() {
        Documento doc = Documento.criar(
                "Relatório de Prestação de Contas 2025",
                TipoDocumento.RELATORIO,
                LocalDate.of(2026, 1, 15),
                "Auditoria Externa",
                "Versão homologada"
        );
        Documento salvo = repositoryPort.salvar(doc);

        salvo.arquivar();
        repositoryPort.salvar(salvo);

        Optional<Documento> recuperado = repositoryPort.buscarPorIdComVersoes(salvo.getId());
        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getStatus()).isEqualTo(StatusDocumento.ARQUIVADO);
    }

    @Test
    @DisplayName("Should filter by tipo=ata and date period 01/01 to 30/06 exclusively (CA4)")
    void should_filterByTipoAndDateRange_CA4() {
        // Documentos que atendem ao critério (ata entre 01/01/2026 e 30/06/2026)
        repositoryPort.salvar(Documento.criar("Ata Reunião Q1", TipoDocumento.ATA, LocalDate.of(2026, 1, 15), "Secretaria", null));
        repositoryPort.salvar(Documento.criar("Ata Reunião Q2", TipoDocumento.ATA, LocalDate.of(2026, 6, 30), "Secretaria", null));

        // Documentos que NÃO devem ser retornados:
        // 1. Ata fora do período (posterior)
        repositoryPort.salvar(Documento.criar("Ata Reunião Q3", TipoDocumento.ATA, LocalDate.of(2026, 7, 1), "Secretaria", null));
        // 2. Ata fora do período (anterior)
        repositoryPort.salvar(Documento.criar("Ata Reunião Antiga", TipoDocumento.ATA, LocalDate.of(2025, 12, 31), "Secretaria", null));
        // 3. Contrato dentro do período (tipo diferente)
        repositoryPort.salvar(Documento.criar("Contrato Parceria", TipoDocumento.CONTRATO, LocalDate.of(2026, 3, 15), "Diretoria", null));
        // 4. Relatório dentro do período (tipo diferente)
        repositoryPort.salvar(Documento.criar("Relatório Financeiro", TipoDocumento.RELATORIO, LocalDate.of(2026, 5, 10), "Tesouraria", null));

        PaginatedResult<Documento> resultado = repositoryPort.buscarComFiltros(
                "ata",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30),
                null,
                0,
                10
        );

        assertThat(resultado.totalElements()).isEqualTo(2);
        assertThat(resultado.content()).hasSize(2);
        assertThat(resultado.content()).allMatch(d -> d.getTipo() == TipoDocumento.ATA);
        assertThat(resultado.content()).extracting(Documento::getTitulo)
                .containsExactlyInAnyOrder("Ata Reunião Q1", "Ata Reunião Q2");
    }

    @Test
    @DisplayName("Should search by partial title case-insensitive")
    void should_searchByPartialTitle() {
        repositoryPort.salvar(Documento.criar("Contrato de Locação Sede", TipoDocumento.CONTRATO, LocalDate.of(2026, 3, 1), "Jurídico", null));
        repositoryPort.salvar(Documento.criar("Ofício Governamental", TipoDocumento.OFICIO, LocalDate.of(2026, 4, 1), "Diretoria", null));

        PaginatedResult<Documento> resultado = repositoryPort.buscarComFiltros(
                null,
                null,
                null,
                "locação",
                0,
                10
        );

        assertThat(resultado.totalElements()).isEqualTo(1);
        assertThat(resultado.content().get(0).getTitulo()).isEqualTo("Contrato de Locação Sede");
    }

    @Test
    @DisplayName("Should calculate consolidated metrics (US06 / RF06)")
    void should_calculateConsolidatedMetrics() {
        Documento d1 = Documento.criar("Ata 1", TipoDocumento.ATA, LocalDate.now(), "Sec", null);
        Documento d2 = Documento.criar("Ata 2", TipoDocumento.ATA, LocalDate.now(), "Sec", null);
        d2.arquivar();
        Documento d3 = Documento.criar("Contrato 1", TipoDocumento.CONTRATO, LocalDate.now(), "Jur", null);

        repositoryPort.salvar(d1);
        repositoryPort.salvar(d2);
        repositoryPort.salvar(d3);

        MetricasArquivo metricas = repositoryPort.obterMetricas();

        assertThat(metricas.total()).isGreaterThanOrEqualTo(3L);
        assertThat(metricas.vigentes()).isGreaterThanOrEqualTo(2L);
        assertThat(metricas.arquivados()).isGreaterThanOrEqualTo(1L);
        assertThat(metricas.porTipo()).containsKey("ata");
        assertThat(metricas.porTipo()).containsKey("contrato");
        assertThat(metricas.porTipo()).containsKey("relatorio");
        assertThat(metricas.porTipo()).containsKey("oficio");
    }
}
