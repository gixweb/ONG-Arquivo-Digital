package com.ong.backend.domain.model;

import com.ong.backend.domain.exception.DocumentoArquivadoException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Documento {

    private final Long id;
    private String titulo;
    private final TipoDocumento tipo;
    private LocalDate dataDocumento;
    private String responsavel;
    private StatusDocumento status;
    private int versaoAtual;
    private final Instant criadoEm;
    private Instant atualizadoEm;
    private final List<VersaoDocumento> versoes;

    public Documento(Long id,
                     String titulo,
                     TipoDocumento tipo,
                     LocalDate dataDocumento,
                     String responsavel,
                     StatusDocumento status,
                     int versaoAtual,
                     Instant criadoEm,
                     Instant atualizadoEm,
                     List<VersaoDocumento> versoes) {
        validarCamposObrigatorios(titulo, tipo, dataDocumento, responsavel);
        this.id = id;
        this.titulo = titulo.trim();
        this.tipo = tipo;
        this.dataDocumento = dataDocumento;
        this.responsavel = responsavel.trim();
        this.status = status != null ? status : StatusDocumento.VIGENTE;
        this.versaoAtual = Math.max(versaoAtual, 1);
        this.criadoEm = criadoEm != null ? criadoEm : Instant.now();
        this.atualizadoEm = atualizadoEm != null ? atualizadoEm : this.criadoEm;
        this.versoes = new ArrayList<>(versoes != null ? versoes : List.of());
    }

    public static Documento criar(String titulo,
                                  TipoDocumento tipo,
                                  LocalDate dataDocumento,
                                  String responsavel,
                                  String observacaoInicial) {
        String observacao = (observacaoInicial != null && !observacaoInicial.isBlank())
                ? observacaoInicial.trim()
                : "Criação inicial do documento.";

        Instant agora = Instant.now();
        Documento documento = new Documento(
                null,
                titulo,
                tipo,
                dataDocumento,
                responsavel,
                StatusDocumento.VIGENTE,
                1,
                agora,
                agora,
                new ArrayList<>()
        );

        VersaoDocumento versaoInicial = new VersaoDocumento(null, null, 1, observacao, null, agora);
        documento.versoes.add(versaoInicial);
        return documento;
    }

    public VersaoDocumento registrarNovaVersao(String observacao) {
        if (this.status == StatusDocumento.ARQUIVADO) {
            throw new DocumentoArquivadoException(this.id);
        }
        if (observacao == null || observacao.isBlank()) {
            throw new IllegalArgumentException("A observação da nova versão é obrigatória.");
        }

        this.versaoAtual++;
        this.atualizadoEm = Instant.now();

        VersaoDocumento novaVersao = new VersaoDocumento(
                null,
                this.id,
                this.versaoAtual,
                observacao.trim(),
                null,
                this.atualizadoEm
        );
        this.versoes.add(novaVersao);
        return novaVersao;
    }

    public void arquivar() {
        this.status = StatusDocumento.ARQUIVADO;
        this.atualizadoEm = Instant.now();
    }

    public void atualizarMetadados(String novoTitulo, LocalDate novaData, String novoResponsavel) {
        if (this.status == StatusDocumento.ARQUIVADO) {
            throw new DocumentoArquivadoException(this.id);
        }
        validarCamposObrigatorios(novoTitulo, this.tipo, novaData, novoResponsavel);
        this.titulo = novoTitulo.trim();
        this.dataDocumento = novaData;
        this.responsavel = novoResponsavel.trim();
        this.atualizadoEm = Instant.now();
    }

    private void validarCamposObrigatorios(String titulo, TipoDocumento tipo, LocalDate dataDocumento, String responsavel) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título do documento é obrigatório.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo do documento é obrigatório.");
        }
        if (dataDocumento == null) {
            throw new IllegalArgumentException("A data do documento é obrigatória.");
        }
        if (responsavel == null || responsavel.isBlank()) {
            throw new IllegalArgumentException("O responsável pelo documento é obrigatório.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public TipoDocumento getTipo() {
        return tipo;
    }

    public LocalDate getDataDocumento() {
        return dataDocumento;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public StatusDocumento getStatus() {
        return status;
    }

    public int getVersaoAtual() {
        return versaoAtual;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public List<VersaoDocumento> getVersoes() {
        return Collections.unmodifiableList(versoes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Documento documento = (Documento) o;
        return Objects.equals(id, documento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
