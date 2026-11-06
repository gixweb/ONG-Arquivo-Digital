package com.ong.backend.infrastructure.persistence.repository;

import com.ong.backend.infrastructure.persistence.entity.DocumentoJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataDocumentoRepository extends JpaRepository<DocumentoJpaEntity, Long>, JpaSpecificationExecutor<DocumentoJpaEntity> {

    @EntityGraph(attributePaths = {"versoes"})
    Optional<DocumentoJpaEntity> findWithVersoesById(Long id);

    @Query("SELECT d.tipo, COUNT(d) FROM DocumentoJpaEntity d GROUP BY d.tipo")
    List<Object[]> countByTipoGroup();

    @Query("SELECT d.status, COUNT(d) FROM DocumentoJpaEntity d GROUP BY d.status")
    List<Object[]> countByStatusGroup();
}
