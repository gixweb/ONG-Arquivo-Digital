package com.ong.backend.web.controller;

import com.ong.backend.application.port.in.*;
import com.ong.backend.domain.model.Documento;
import com.ong.backend.domain.model.PaginatedResult;
import com.ong.backend.domain.model.VersaoDocumento;
import com.ong.backend.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documentos")
@CrossOrigin(origins = "*")
public class DocumentoController {

    private final CriarDocumentoUseCase criarDocumentoUseCase;
    private final BuscarDocumentoPorIdUseCase buscarDocumentoPorIdUseCase;
    private final RegistrarNovaVersaoUseCase registrarNovaVersaoUseCase;
    private final ArquivarDocumentoUseCase arquivarDocumentoUseCase;
    private final ConsultarHistoricoVersoesUseCase consultarHistoricoVersoesUseCase;
    private final BuscarDocumentosComFiltroUseCase buscarDocumentosComFiltroUseCase;

    public DocumentoController(CriarDocumentoUseCase criarDocumentoUseCase,
                               BuscarDocumentoPorIdUseCase buscarDocumentoPorIdUseCase,
                               RegistrarNovaVersaoUseCase registrarNovaVersaoUseCase,
                               ArquivarDocumentoUseCase arquivarDocumentoUseCase,
                               ConsultarHistoricoVersoesUseCase consultarHistoricoVersoesUseCase,
                               BuscarDocumentosComFiltroUseCase buscarDocumentosComFiltroUseCase) {
        this.criarDocumentoUseCase = criarDocumentoUseCase;
        this.buscarDocumentoPorIdUseCase = buscarDocumentoPorIdUseCase;
        this.registrarNovaVersaoUseCase = registrarNovaVersaoUseCase;
        this.arquivarDocumentoUseCase = arquivarDocumentoUseCase;
        this.consultarHistoricoVersoesUseCase = consultarHistoricoVersoesUseCase;
        this.buscarDocumentosComFiltroUseCase = buscarDocumentosComFiltroUseCase;
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

    @GetMapping
    public ResponseEntity<PaginatedResponse<DocumentoResponse>> listarComFiltros(
            @RequestParam(required = false) String tipo,
            @RequestParam(name = "data_inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(name = "data_fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String titulo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        BuscarDocumentosFiltroQuery query = new BuscarDocumentosFiltroQuery(
                tipo,
                dataInicio,
                dataFim,
                titulo,
                page,
                size
        );

        PaginatedResult<Documento> resultado = buscarDocumentosComFiltroUseCase.buscarComFiltros(query);
        PaginatedResponse<DocumentoResponse> response = PaginatedResponse.fromDomain(resultado, DocumentoResponse::fromDomain);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoDetalhadoResponse> buscarPorId(@PathVariable Long id) {
        Documento documento = buscarDocumentoPorIdUseCase.buscarPorId(id);
        return ResponseEntity.ok(DocumentoDetalhadoResponse.fromDomain(documento));
    }

    @PostMapping("/{id}/versoes")
    public ResponseEntity<VersaoResponse> registrarNovaVersao(
            @PathVariable Long id,
            @Valid @RequestBody NovaVersaoRequest request) {
        VersaoDocumento versaoCriada = registrarNovaVersaoUseCase.registrarNovaVersao(id, request.observacao());
        return ResponseEntity.status(HttpStatus.CREATED).body(VersaoResponse.fromDomain(versaoCriada));
    }

    @PatchMapping("/{id}/arquivar")
    public ResponseEntity<DocumentoResponse> arquivar(@PathVariable Long id) {
        Documento arquivado = arquivarDocumentoUseCase.arquivar(id);
        return ResponseEntity.ok(DocumentoResponse.fromDomain(arquivado));
    }

    @GetMapping("/{id}/versoes")
    public ResponseEntity<List<VersaoResponse>> consultarHistorico(@PathVariable Long id) {
        List<VersaoDocumento> versoes = consultarHistoricoVersoesUseCase.consultarHistorico(id);
        List<VersaoResponse> response = versoes.stream().map(VersaoResponse::fromDomain).toList();
        return ResponseEntity.ok(response);
    }
}
