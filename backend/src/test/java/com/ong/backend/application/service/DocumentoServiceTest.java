package com.ong.backend.application.service;

import com.ong.backend.application.port.in.CriarDocumentoCommand;
import com.ong.backend.application.port.out.DocumentoRepositoryPort;
import com.ong.backend.domain.exception.DocumentoArquivadoException;
import com.ong.backend.domain.exception.DocumentoNaoEncontradoException;
import com.ong.backend.domain.exception.TipoDocumentoInvalidoException;
import com.ong.backend.domain.model.Documento;
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
        assertThat(criado.getVersaoAtual()).isEqualTo(1);
        verify(repositoryPort).salvar(any(Documento.class));
    }

    @Test
    @DisplayName("Should throw TipoDocumentoInvalidoException when tipo is memorando (CA1)")
    void should_throwException_when_tipoIsMemorando_CA1() {
        CriarDocumentoCommand command = new CriarDocumentoCommand(
                "Memorando Interno 01",
                "memorando",
                LocalDate.now(),
                "Gerência",
                null
        );

        assertThatThrownBy(() -> service.criar(command))
                .isInstanceOf(TipoDocumentoInvalidoException.class)
                .hasMessageContaining("memorando");
    }

    @Test
    @DisplayName("Should return document when found by id")
    void should_returnDocument_when_foundById() {
        Documento doc = Documento.criar("Contrato X", TipoDocumento.CONTRATO, LocalDate.now(), "Diretoria", null);
        when(repositoryPort.buscarPorIdComVersoes(1L)).thenReturn(Optional.of(doc));

        Documento resultado = service.buscarPorId(1L);

        assertThat(resultado).isEqualTo(doc);
    }

    @Test
    @DisplayName("Should throw DocumentoNaoEncontradoException when id does not exist")
    void should_throwException_when_notFoundById() {
        when(repositoryPort.buscarPorIdComVersoes(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(DocumentoNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should register new version incrementing versao_atual (CA2)")
    void should_registerNewVersion_CA2() {
        Documento doc = Documento.criar("Ata Q1", TipoDocumento.ATA, LocalDate.now(), "Secretaria", "Inicial");
        when(repositoryPort.buscarPorIdComVersoes(1L)).thenReturn(Optional.of(doc));
        when(repositoryPort.salvar(any(Documento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VersaoDocumento novaVersao = service.registrarNovaVersao(1L, "Revisão ortográfica");

        assertThat(novaVersao.getNumero()).isEqualTo(2);
        assertThat(novaVersao.getObservacao()).isEqualTo("Revisão ortográfica");
        assertThat(doc.getVersaoAtual()).isEqualTo(2);
        assertThat(doc.getVersoes()).hasSize(2);
        verify(repositoryPort).salvar(doc);
    }

    @Test
    @DisplayName("Should throw DocumentoArquivadoException when registering version on archived doc (CA3)")
    void should_throwException_when_documentIsArchived_CA3() {
        Documento doc = Documento.criar("Relatório Anual", TipoDocumento.RELATORIO, LocalDate.now(), "Financeiro", "Final");
        doc.arquivar();
        when(repositoryPort.buscarPorIdComVersoes(1L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> service.registrarNovaVersao(1L, "Tentativa"))
                .isInstanceOf(DocumentoArquivadoException.class);
    }

    @Test
    @DisplayName("Should archive document successfully")
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
}
