package com.ong.backend.application.port.out;

import com.ong.backend.domain.model.Documento;
import com.ong.backend.domain.model.VersaoDocumento;

import java.util.List;
import java.util.Optional;

public interface DocumentoRepositoryPort {

    Documento salvar(Documento documento);

    Optional<Documento> buscarPorId(Long id);

    Optional<Documento> buscarPorIdComVersoes(Long id);

    List<VersaoDocumento> listarVersoesPorDocumentoId(Long documentoId);

    boolean existePorId(Long id);

    void excluir(Long id);
}
