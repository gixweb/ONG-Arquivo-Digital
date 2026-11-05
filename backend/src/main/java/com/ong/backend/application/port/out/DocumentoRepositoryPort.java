package com.ong.backend.application.port.out;

import com.ong.backend.domain.model.Documento;

import java.util.Optional;

public interface DocumentoRepositoryPort {

    Documento salvar(Documento documento);

    Optional<Documento> buscarPorId(Long id);

    Optional<Documento> buscarPorIdComVersoes(Long id);

    boolean existePorId(Long id);

    void excluir(Long id);
}
