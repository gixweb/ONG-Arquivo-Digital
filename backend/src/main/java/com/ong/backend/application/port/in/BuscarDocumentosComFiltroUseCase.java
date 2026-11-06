package com.ong.backend.application.port.in;

import com.ong.backend.domain.model.Documento;
import com.ong.backend.domain.model.PaginatedResult;

public interface BuscarDocumentosComFiltroUseCase {
    PaginatedResult<Documento> buscarComFiltros(BuscarDocumentosFiltroQuery query);
}
