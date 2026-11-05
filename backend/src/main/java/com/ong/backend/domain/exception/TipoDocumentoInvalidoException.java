package com.ong.backend.domain.exception;

public class TipoDocumentoInvalidoException extends DomainException {

    public TipoDocumentoInvalidoException(String tipoInformado) {
        super(String.format("Tipo de documento inválido: '%s'. Os tipos permitidos são: ata, contrato, relatorio, oficio.", tipoInformado));
    }
}
