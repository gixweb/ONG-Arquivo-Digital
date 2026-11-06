package com.ong.backend.application.service;

import com.ong.backend.application.port.in.CriarDocumentoCommand;
import com.ong.backend.application.port.out.DocumentoRepositoryPort;
import com.ong.backend.domain.exception.DocumentoArquivadoException;
import com.ong.backend.domain.exception.DocumentoNaoEncontradoException;
import com.ong.backend.domain.exception.TipoDocumentoInvalidoException;
import com.ong.backend.domain.model.Documento;
import com.ong.backend.domain.model.MetricasArquivo;
import com.ong.backend.domain.model.StatusDocumento;
import com.ong.backend.domain.model.TipoDocumento;
import com.ong.backend.domain.model.VersaoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock
    private DocumentoRepositoryPort repositoryPort;

    private DocumentoService service;

    @BeforeEach
    void setUp() {
        service = new DocumentoService(repositoryPort);
    }

    @Test
    @DisplayName("Should create document and call repository port")
    void should_createDocument_successfully() {
        CriarDocumentoCommand command = new CriarDocumentoCommand(
                "Ata de Fundação",
                "ata",
                LocalDate.of(2026, 1, 1),
                "Presidente",
                "Registro inicial"
        );

        when(repositoryPort.salvar(any(Documento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Documento criado = service.criar(command);

        assertThat(criado).isNotNull();
        assertThat(criado.getTitulo()).isEqualTo("Ata de Fundação");
        assertThat(criado.getTipo()).isEqualTo(TipoDocumento.ATA);
        assertThat(criado.getStatus()).isEqualTo(StatusDocumento.VIGENTE);
        assertThat(criado.getVersaoAtual()).isEqualTo(1);
        verify(repositoryPort).salvar(any(Documento.class));
    }

    @Test
    @DisplayName("Should reject invalid document type")
    void should_rejectInvalidTipo() {
        CriarDocumentoCommand command = new CriarDocumentoCommand(
                "Memorando Interno",
                "memorando",
                LocalDate.of(2026, 1, 1),
                "Secretário",
                null
        );

        assertThatThrownBy(() -> service.criar(command))
                .isInstanceOf(TipoDocumentoInvalidoException.class);
    }

    @Test
    @DisplayName("Should find document by id or throw 404 domain exception")
    void should_findDocumentById() {
        Documento doc = Documento.criar("Contrato 01", TipoDocumento.CONTRATO, LocalDate.now(), "Admin", null);
        when(repositoryPort.buscarPorIdComVersoes(1L)).thenReturn(Optional.of(doc));
        when(repositoryPort.buscarPorIdComVersoes(99L)).thenReturn(Optional.empty());

        Documento found = service.buscarPorId(1L);
        assertThat(found).isEqualTo(doc);

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(DocumentoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Should register new version on active document")
    void should_registerNewVersion() {
        Documento doc = Documento.criar("Contrato 01", TipoDocumento.CONTRATO, LocalDate.now(), "Admin", "Inicial");
        when(repositoryPort.buscarPorIdComVersoes(1L)).thenReturn(Optional.of(doc));
        when(repositoryPort.salvar(any(Documento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VersaoDocumento versao2 = service.registrarNovaVersao(1L, "Revisão cláusula 3");

        assertThat(versao2.getNumero()).isEqualTo(2);
        assertThat(versao2.getObservacao()).isEqualTo("Revisão cláusula 3");
        assertThat(doc.getVersaoAtual()).isEqualTo(2);
        assertThat(doc.getVersoes()).hasSize(2);
        verify(repositoryPort).salvar(doc);
    }

    @Test
    @DisplayName("Should reject new version when document is archived")
    void should_rejectNewVersion_whenArchived() {
        Documento doc = Documento.criar("Relatório Anual", TipoDocumento.RELATORIO, LocalDate.now(), "Diretoria", null);
        doc.arquivar();
        when(repositoryPort.buscarPorIdComVersoes(1L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> service.registrarNovaVersao(1L, "Tentativa de alteração"))
                .isInstanceOf(DocumentoArquivadoException.class);
    }

    @Test
    @DisplayName("Should archive active document")
    void should_archiveDocument() {
        Documento doc = Documento.criar("Ofício 02", TipoDocumento.OFICIO, LocalDate.now(), "Diretoria", null);
        when(repositoryPort.buscarPorIdComVersoes(1L)).thenReturn(Optional.of(doc));
        when(repositoryPort.salvar(any(Documento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Documento arquivado = service.arquivar(1L);

        assertThat(arquivado.getStatus()).isEqualTo(StatusDocumento.ARQUIVADO);
        verify(repositoryPort).salvar(doc);
    }

    @Test
    @DisplayName("Should return list of versions for document")
    void should_returnHistory() {
        VersaoDocumento v1 = new VersaoDocumento(1L, 5L, 1, "Criado", null, null);
        VersaoDocumento v2 = new VersaoDocumento(2L, 5L, 2, "Atualizado", null, null);
        when(repositoryPort.existePorId(5L)).thenReturn(true);
        when(repositoryPort.listarVersoesPorDocumentoId(5L)).thenReturn(List.of(v1, v2));

        List<VersaoDocumento> historico = service.consultarHistorico(5L);

        assertThat(historico).hasSize(2);
        assertThat(historico.get(0).getNumero()).isEqualTo(1);
        assertThat(historico.get(1).getNumero()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return consolidated metrics from repository")
    void should_returnMetrics() {
        MetricasArquivo metricas = new MetricasArquivo(10L, 8L, 2L, Map.of("ata", 4L, "contrato", 3L, "relatorio", 2L, "oficio", 1L));
        when(repositoryPort.obterMetricas()).thenReturn(metricas);

        MetricasArquivo resultado = service.obterMetricas();

        assertThat(resultado.total()).isEqualTo(10L);
        assertThat(resultado.vigentes()).isEqualTo(8L);
        assertThat(resultado.arquivados()).isEqualTo(2L);
        assertThat(resultado.porTipo()).containsEntry("ata", 4L);
        verify(repositoryPort).obterMetricas();
    }
}
