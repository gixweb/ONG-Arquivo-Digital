package com.ong.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(name = "data_documento", nullable = false)
    private LocalDate dataDocumento;

    @Column(nullable = false, length = 150)
    private String responsavel;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "versao_atual", nullable = false)
    private int versaoAtual = 1;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();

    @OneToMany(mappedBy = "documento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("numero ASC")
    private List<VersaoJpaEntity> versoes = new ArrayList<>();

    public void adicionarVersao(VersaoJpaEntity versao) {
        versoes.add(versao);
        versao.setDocumento(this);
    }
}
