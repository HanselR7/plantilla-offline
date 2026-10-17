package com.plantilla.offline.adapter.out.persistence.nota;

import com.plantilla.offline.adapter.out.persistence.base.JpaRepositoryBase;
import com.plantilla.offline.application.nota.NotaRepository;
import com.plantilla.offline.domain.nota.Nota;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.plantilla.offline.adapter.out.persistence.base.OutboxWriter;
import jakarta.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
@Transactional
public class NotaJpaRepository implements NotaRepository {

    @PersistenceContext(unitName = "plantillaPU")
    private EntityManager em;

    @Inject
    private OutboxWriter outbox;

    private JpaRepositoryBase<NotaEntity> base;

    @PostConstruct
    void init() {
        base = new JpaRepositoryBase<>(em, NotaEntity.class);
    }

    @Override
    public Nota save(Nota nota) {
        NotaEntity entity = nota.getId() == null ? new NotaEntity() : em.find(NotaEntity.class, nota.getId());
        if (entity == null) {
            entity = new NotaEntity();
            entity.setId(nota.getId());
        }
        entity.setTitulo(nota.getTitulo());
        entity.setContenido(nota.getContenido());
        NotaEntity saved = base.saveEntity(entity);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", saved.getId().toString());
        payload.put("titulo", saved.getTitulo());
        payload.put("contenido", saved.getContenido());
        outbox.record("nota", saved.getId(), "UPSERT", payload);

        return toDomain(saved);
    }

    @Override
    public boolean softDelete(UUID id) {
        boolean deleted = base.softDeleteEntity(id);
        if (deleted) {
            outbox.record("nota", id, "DELETE", Map.of("id", id.toString()));
        }
        return deleted;
    }

    @Override
    public Optional<Nota> findById(UUID id) {
        return base.findEntity(id).map(this::toDomain);
    }

    @Override
    public List<Nota> findAll() {
        return base.findAllEntities().stream().map(this::toDomain).toList();
    }

    private Nota toDomain(NotaEntity e) {
        return new Nota(e.getId(), e.getTitulo(), e.getContenido());
    }
}