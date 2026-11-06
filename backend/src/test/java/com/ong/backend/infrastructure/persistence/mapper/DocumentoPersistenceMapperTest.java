package com.ong.backend.infrastructure.persistence.mapper;

import com.ong.backend.domain.model.Documento;
import com.ong.backend.domain.model.StatusDocumento;
import com.ong.backend.domain.model.TipoDocumento;
import com.ong.backend.domain.model.VersaoDocumento;
import com.ong.backend.infrastructure.persistence.entity.DocumentoJpaEntity;
import com.ong.backend.infrastructure.persistence.entity.VersaoJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentoPersistenceMapperTest {

    private DocumentoPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DocumentoPersistenceMapper();
    }

    @Test
    @DisplayName("Should return null when toDomain received null entity")
    void should_returnNull_when_toDomainNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("Should return null when toDomainVersao received null entity")
    void should_returnNull_when_toDomainVersaoNull() {
        assertThat(mapper.toDomainVersao(null)).isNull();
    }

    @Test
    @DisplayName("Should map entity with null versoes list to domain with empty versoes list")
    void should_mapEntity_withNullVersoes_toDomainWithEmptyVersoes() {
        DocumentoJpaEntity entity = new DocumentoJpaEntity();
        entity.setId(1L);
        entity.setTitulo("Título");
        entity.setTipo("ata");
        entity.setDataDocumento(LocalDate.now());
        entity.setResponsavel("Resp");
        entity.setStatus("vigente");
        entity.setVersaoAtual(1);
        entity.setCriadoEm(Instant.now());
        entity.setAtualizadoEm(Instant.now());
        entity.setVersoes(null);

        Documento doc = mapper.toDomain(entity);

        assertThat(doc).isNotNull();
        assertThat(doc.getVersoes()).isEmpty();
    }

    @Test
    @DisplayName("Should map versao entity with null documento parent")
    void should_mapVersaoEntity_withNullDocumentoParent() {
        VersaoJpaEntity versaoEntity = new VersaoJpaEntity();
        versaoEntity.setId(10L);
        versaoEntity.setDocumento(null);
        versaoEntity.setNumero(1);
        versaoEntity.setObservacao("Obs");
        versaoEntity.setBlobNome(null);
        versaoEntity.setCriadaEm(Instant.now());

        VersaoDocumento versaoDoc = mapper.toDomainVersao(versaoEntity);

        assertThat(versaoDoc).isNotNull();
        assertThat(versaoDoc.getDocumentoId()).isNull();
    }

    @Test
    @DisplayName("Should return null when toJpaEntity received null")
    void should_returnNull_when_toJpaEntityNull() {
        assertThat(mapper.toJpaEntity(null)).isNull();
    }

    @Test
    @DisplayName("Should return null when toJpaVersao received null")
    void should_returnNull_when_toJpaVersaoNull() {
        assertThat(mapper.toJpaVersao(null, new DocumentoJpaEntity())).isNull();
    }
}
