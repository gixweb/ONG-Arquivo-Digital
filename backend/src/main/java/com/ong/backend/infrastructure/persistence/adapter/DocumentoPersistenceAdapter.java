package com.ong.backend.infrastructure.persistence.adapter;

import com.ong.backend.application.port.out.DocumentoRepositoryPort;
import com.ong.backend.domain.model.Documento;
import com.ong.backend.domain.model.VersaoDocumento;
import com.ong.backend.infrastructure.persistence.entity.DocumentoJpaEntity;
import com.ong.backend.infrastructure.persistence.mapper.DocumentoPersistenceMapper;
import com.ong.backend.infrastructure.persistence.repository.SpringDataDocumentoRepository;
import com.ong.backend.infrastructure.persistence.repository.SpringDataVersaoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DocumentoPersistenceAdapter implements DocumentoRepositoryPort {

    private final SpringDataDocumentoRepository jpaRepository;
    private final SpringDataVersaoRepository versaoRepository;
    private final DocumentoPersistenceMapper mapper;

    public DocumentoPersistenceAdapter(SpringDataDocumentoRepository jpaRepository,
                                       SpringDataVersaoRepository versaoRepository,
                                       DocumentoPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.versaoRepository = versaoRepository;
        this.mapper = mapper;
    }

    @Override
    public Documento salvar(Documento documento) {
        DocumentoJpaEntity entity = mapper.toJpaEntity(documento);
        DocumentoJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Documento> buscarPorId(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Documento> buscarPorIdComVersoes(Long id) {
        return jpaRepository.findWithVersoesById(id).map(mapper::toDomain);
    }

    @Override
    public List<VersaoDocumento> listarVersoesPorDocumentoId(Long documentoId) {
        return versaoRepository.findByDocumentoIdOrderByNumeroAsc(documentoId).stream()
                .map(mapper::toDomainVersao)
                .toList();
    }

    @Override
    public boolean existePorId(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void excluir(Long id) {
        jpaRepository.deleteById(id);
    }
}
