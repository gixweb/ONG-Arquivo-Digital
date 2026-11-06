package com.ong.backend.web.controller;

import com.ong.backend.application.port.in.BuscarDocumentoPorIdUseCase;
import com.ong.backend.application.port.in.CriarDocumentoCommand;
import com.ong.backend.application.port.in.CriarDocumentoUseCase;
import com.ong.backend.domain.model.Documento;
import com.ong.backend.web.dto.CriarDocumentoRequest;
import com.ong.backend.web.dto.DocumentoDetalhadoResponse;
import com.ong.backend.web.dto.DocumentoResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/documentos")
@CrossOrigin(origins = "*") // configurável para console web
public class DocumentoController {

    private final CriarDocumentoUseCase criarDocumentoUseCase;
    private final BuscarDocumentoPorIdUseCase buscarDocumentoPorIdUseCase;

    public DocumentoController(CriarDocumentoUseCase criarDocumentoUseCase,
                               BuscarDocumentoPorIdUseCase buscarDocumentoPorIdUseCase) {
        this.criarDocumentoUseCase = criarDocumentoUseCase;
        this.buscarDocumentoPorIdUseCase = buscarDocumentoPorIdUseCase;
    }

    @PostMapping
    public ResponseEntity<DocumentoResponse> criar(@Valid @RequestBody CriarDocumentoRequest request) {
        CriarDocumentoCommand command = new CriarDocumentoCommand(
                request.titulo(),
                request.tipo(),
                request.dataDocumento(),
                request.responsavel(),
                request.observacaoInicial()
        );

        Documento criado = criarDocumentoUseCase.criar(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();

        return ResponseEntity.created(location).body(DocumentoResponse.fromDomain(criado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoDetalhadoResponse> buscarPorId(@PathVariable Long id) {
        Documento documento = buscarDocumentoPorIdUseCase.buscarPorId(id);
        return ResponseEntity.ok(DocumentoDetalhadoResponse.fromDomain(documento));
    }
}
