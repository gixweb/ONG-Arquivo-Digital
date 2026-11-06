package com.ong.backend.domain.exception;

public class DocumentoNaoEncontradoException extends DomainException {

    public DocumentoNaoEncontradoException(Long id) {
        super(String.format("Documento com id %d não foi encontrado.", id));
    }
}
