package com.ong.backend.application.service;

import com.ong.backend.application.port.in.*;
import com.ong.backend.application.port.out.DocumentoRepositoryPort;
import com.ong.backend.domain.exception.DocumentoNaoEncontradoException;
import com.ong.backend.domain.model.Documento;
import com.ong.backend.domain.model.MetricasArquivo;
import com.ong.backend.domain.model.PaginatedResult;
import com.ong.backend.domain.model.TipoDocumento;
import com.ong.backend.domain.model.VersaoDocumento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocumentoService implements
        CriarDocumentoUseCase,
        BuscarDocumentoPorIdUseCase,
        RegistrarNovaVersaoUseCase,
        ArquivarDocumentoUseCase,
        ConsultarHistoricoVersoesUseCase,
        BuscarDocumentosComFiltroUseCase,
        ObterMetricasArquivoUseCase {

    private final DocumentoRepositoryPort documentoRepository;

    public DocumentoService(DocumentoRepositoryPort documentoRepository) {
        this.documentoRepository = documentoRepository;
    }

    @Override
    @Transactional
    public Documento criar(CriarDocumentoCommand command) {
        TipoDocumento tipo = TipoDocumento.from(command.tipo());

        Documento documento = Documento.criar(
                command.titulo(),
                tipo,
                command.dataDocumento(),
                command.responsavel(),
                command.observacaoInicial()
        );

        return documentoRepository.salvar(documento);
    }

    @Override
    @Transactional(readOnly = true)
    public Documento buscarPorId(Long id) {
        return documentoRepository.buscarPorIdComVersoes(id)
                .orElseThrow(() -> new DocumentoNaoEncontradoException(id));
    }

    @Override
    @Transactional
    public VersaoDocumento registrarNovaVersao(Long documentoId, String observacao) {
        Documento documento = buscarPorId(documentoId);
        VersaoDocumento novaVersao = documento.registrarNovaVersao(observacao);
        documentoRepository.salvar(documento);
        return novaVersao;
    }

    @Override
    @Transactional
    public Documento arquivar(Long documentoId) {
        Documento documento = buscarPorId(documentoId);
        documento.arquivar();
        return documentoRepository.salvar(documento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VersaoDocumento> consultarHistorico(Long documentoId) {
        if (!documentoRepository.existePorId(documentoId)) {
            throw new DocumentoNaoEncontradoException(documentoId);
        }
        return documentoRepository.listarVersoesPorDocumentoId(documentoId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<Documento> buscarComFiltros(BuscarDocumentosFiltroQuery query) {
        return documentoRepository.buscarComFiltros(
                query.tipo(),
                query.dataInicio(),
                query.dataFim(),
                query.titulo(),
                query.page(),
                query.size()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MetricasArquivo obterMetricas() {
        return documentoRepository.obterMetricas();
    }
}
