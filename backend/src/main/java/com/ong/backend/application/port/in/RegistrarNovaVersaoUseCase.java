package com.ong.backend.application.port.in;

import com.ong.backend.domain.model.VersaoDocumento;

public interface RegistrarNovaVersaoUseCase {
    VersaoDocumento registrarNovaVersao(Long documentoId, String observacao);
}
