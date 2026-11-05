package com.ong.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "versoes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_documento_versao", columnNames = {"documento_id", "numero"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VersaoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "documento_id", nullable = false)
    private DocumentoJpaEntity documento;

    @Column(nullable = false)
    private int numero;

    @Column(nullable = false, length = 500)
    private String observacao;

    @Column(name = "blob_nome", length = 255)
    private String blobNome;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private Instant criadaEm = Instant.now();
}
