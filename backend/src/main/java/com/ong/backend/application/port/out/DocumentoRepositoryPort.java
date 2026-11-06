package com.ong.backend.application.port.out;

import com.ong.backend.domain.model.Documento;
import com.ong.backend.domain.model.PaginatedResult;
import com.ong.backend.domain.model.VersaoDocumento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DocumentoRepositoryPort {

    Documento salvar(Documento documento);

    Optional<Documento> buscarPorId(Long id);

    Optional<Documento> buscarPorIdComVersoes(Long id);

    List<VersaoDocumento> listarVersoesPorDocumentoId(Long documentoId);

    PaginatedResult<Documento> buscarComFiltros(
            String tipo,
            LocalDate dataInicio,
            LocalDate dataFim,
            String titulo,
            int page,
            int size
    );

    boolean existePorId(Long id);

    void excluir(Long id);
}
