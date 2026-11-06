package com.ong.backend.infrastructure.persistence.adapter;

import com.ong.backend.application.port.out.DocumentoRepositoryPort;
import com.ong.backend.domain.model.Documento;
import com.ong.backend.domain.model.MetricasArquivo;
import com.ong.backend.domain.model.PaginatedResult;
import com.ong.backend.domain.model.TipoDocumento;
import com.ong.backend.domain.model.VersaoDocumento;
import com.ong.backend.infrastructure.persistence.entity.DocumentoJpaEntity;
import com.ong.backend.infrastructure.persistence.mapper.DocumentoPersistenceMapper;
import com.ong.backend.infrastructure.persistence.repository.SpringDataDocumentoRepository;
import com.ong.backend.infrastructure.persistence.repository.SpringDataVersaoRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    public PaginatedResult<Documento> buscarComFiltros(String tipo,
                                                       LocalDate dataInicio,
                                                       LocalDate dataFim,
                                                       String titulo,
                                                       int page,
                                                       int size) {
        Specification<DocumentoJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (tipo != null && !tipo.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("tipo")), tipo.trim().toLowerCase()));
            }

            if (dataInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dataDocumento"), dataInicio));
            }

            if (dataFim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dataDocumento"), dataFim));
            }

            if (titulo != null && !titulo.isBlank()) {
                String pattern = "%" + titulo.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("titulo")), pattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataDocumento", "id"));
        Page<DocumentoJpaEntity> pagedEntities = jpaRepository.findAll(spec, pageRequest);

        List<Documento> content = pagedEntities.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return new PaginatedResult<>(
                content,
                pagedEntities.getNumber(),
                pagedEntities.getSize(),
                pagedEntities.getTotalElements(),
                pagedEntities.getTotalPages()
        );
    }

    @Override
    public MetricasArquivo obterMetricas() {
        long total = jpaRepository.count();

        Map<String, Long> statusMap = new HashMap<>();
        statusMap.put("vigente", 0L);
        statusMap.put("arquivado", 0L);
        for (Object[] row : jpaRepository.countByStatusGroup()) {
            if (row != null && row.length >= 2) {
                String status = (String) row[0];
                Long count = ((Number) row[1]).longValue();
                statusMap.put(status, count);
            }
        }

        Map<String, Long> tipoMap = new LinkedHashMap<>();
        for (TipoDocumento tipo : TipoDocumento.values()) {
            tipoMap.put(tipo.getCodigo(), 0L);
        }
        for (Object[] row : jpaRepository.countByTipoGroup()) {
            if (row != null && row.length >= 2) {
                String tipo = (String) row[0];
                Long count = ((Number) row[1]).longValue();
                tipoMap.put(tipo, count);
            }
        }

        return new MetricasArquivo(
                total,
                statusMap.getOrDefault("vigente", 0L),
                statusMap.getOrDefault("arquivado", 0L),
                tipoMap
        );
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
