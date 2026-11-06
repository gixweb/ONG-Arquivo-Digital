package com.ong.backend.application.port.in;

import com.ong.backend.domain.model.Documento;

public interface CriarDocumentoUseCase {
    Documento criar(CriarDocumentoCommand command);
}
