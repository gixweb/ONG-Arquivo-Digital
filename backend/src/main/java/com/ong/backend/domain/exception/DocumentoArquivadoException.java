package com.ong.backend.domain.exception;

public class DocumentoArquivadoException extends DomainException {

    public DocumentoArquivadoException(Long id) {
        super(String.format("O documento %d está arquivado e não pode receber novas versões.", id));
    }
}
