package com.ong.backend.infrastructure.persistence.repository;

import com.ong.backend.infrastructure.persistence.entity.DocumentoJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataDocumentoRepository extends JpaRepository<DocumentoJpaEntity, Long>, JpaSpecificationExecutor<DocumentoJpaEntity> {

    @EntityGraph(attributePaths = {"versoes"})
    Optional<DocumentoJpaEntity> findWithVersoesById(Long id);
}
