package com.ong.backend.infrastructure.persistence.mapper;

import com.ong.backend.domain.model.Documento;
import com.ong.backend.domain.model.StatusDocumento;
import com.ong.backend.domain.model.TipoDocumento;
import com.ong.backend.domain.model.VersaoDocumento;
import com.ong.backend.infrastructure.persistence.entity.DocumentoJpaEntity;
import com.ong.backend.infrastructure.persistence.entity.VersaoJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DocumentoPersistenceMapper {

    public Documento toDomain(DocumentoJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        List<VersaoDocumento> versoes = entity.getVersoes() != null
                ? entity.getVersoes().stream().map(this::toDomainVersao).collect(Collectors.toList())
                : new ArrayList<>();

        return new Documento(
                entity.getId(),
                entity.getTitulo(),
                TipoDocumento.from(entity.getTipo()),
                entity.getDataDocumento(),
                entity.getResponsavel(),
                StatusDocumento.from(entity.getStatus()),
                entity.getVersaoAtual(),
                entity.getCriadoEm(),
                entity.getAtualizadoEm(),
                versoes
        );
    }

    public VersaoDocumento toDomainVersao(VersaoJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Long docId = entity.getDocumento() != null ? entity.getDocumento().getId() : null;
        return new VersaoDocumento(
                entity.getId(),
                docId,
                entity.getNumero(),
                entity.getObservacao(),
                entity.getBlobNome(),
                entity.getCriadaEm()
        );
    }

    public DocumentoJpaEntity toJpaEntity(Documento domain) {
        if (domain == null) {
            return null;
        }
        DocumentoJpaEntity entity = new DocumentoJpaEntity();
        entity.setId(domain.getId());
        entity.setTitulo(domain.getTitulo());
        entity.setTipo(domain.getTipo().getCodigo());
        entity.setDataDocumento(domain.getDataDocumento());
        entity.setResponsavel(domain.getResponsavel());
        entity.setStatus(domain.getStatus().getCodigo());
        entity.setVersaoAtual(domain.getVersaoAtual());
        entity.setCriadoEm(domain.getCriadoEm());
        entity.setAtualizadoEm(domain.getAtualizadoEm());

        if (domain.getVersoes() != null) {
            List<VersaoJpaEntity> jpaVersoes = domain.getVersoes().stream()
                    .map(v -> toJpaVersao(v, entity))
                    .collect(Collectors.toList());
            entity.setVersoes(jpaVersoes);
        }
        return entity;
    }

    public VersaoJpaEntity toJpaVersao(VersaoDocumento domainVersao, DocumentoJpaEntity parent) {
        if (domainVersao == null) {
            return null;
        }
        VersaoJpaEntity entity = new VersaoJpaEntity();
        entity.setId(domainVersao.getId());
        entity.setDocumento(parent);
        entity.setNumero(domainVersao.getNumero());
        entity.setObservacao(domainVersao.getObservacao());
        entity.setBlobNome(domainVersao.getBlobNome());
        entity.setCriadaEm(domainVersao.getCriadaEm());
        return entity;
    }
}
