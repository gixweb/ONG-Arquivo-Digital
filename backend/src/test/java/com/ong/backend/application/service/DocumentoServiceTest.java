package com.ong.backend.application.service;

import com.ong.backend.application.port.in.CriarDocumentoCommand;
import com.ong.backend.application.port.out.DocumentoRepositoryPort;
import com.ong.backend.domain.exception.DocumentoNaoEncontradoException;
import com.ong.backend.domain.exception.TipoDocumentoInvalidoException;
import com.ong.backend.domain.model.Documento;
import com.ong.backend.domain.model.TipoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
}
