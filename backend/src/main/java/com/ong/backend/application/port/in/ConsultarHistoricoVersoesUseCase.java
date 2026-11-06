package com.ong.backend.application.port.in;

import com.ong.backend.domain.model.VersaoDocumento;

import java.util.List;

public interface ConsultarHistoricoVersoesUseCase {
    List<VersaoDocumento> consultarHistorico(Long documentoId);
}
