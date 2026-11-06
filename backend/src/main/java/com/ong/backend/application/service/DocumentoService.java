package com.ong.backend.application.service;

import com.ong.backend.application.port.in.BuscarDocumentoPorIdUseCase;
import com.ong.backend.application.port.in.CriarDocumentoCommand;
import com.ong.backend.application.port.in.CriarDocumentoUseCase;
import com.ong.backend.application.port.out.DocumentoRepositoryPort;
import com.ong.backend.domain.exception.DocumentoNaoEncontradoException;
import com.ong.backend.domain.model.Documento;
import com.ong.backend.domain.model.TipoDocumento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentoService implements CriarDocumentoUseCase, BuscarDocumentoPorIdUseCase {

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
}
