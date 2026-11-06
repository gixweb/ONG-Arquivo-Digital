package com.ong.backend.infrastructure.persistence.repository;

import com.ong.backend.infrastructure.persistence.entity.VersaoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataVersaoRepository extends JpaRepository<VersaoJpaEntity, Long> {

    List<VersaoJpaEntity> findByDocumentoIdOrderByNumeroAsc(Long documentoId);
}
