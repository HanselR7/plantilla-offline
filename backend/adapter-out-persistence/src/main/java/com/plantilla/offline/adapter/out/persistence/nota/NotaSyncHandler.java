package com.plantilla.offline.adapter.out.persistence.nota;

import com.plantilla.offline.application.sync.SyncChange;
import com.plantilla.offline.application.sync.SyncHandler;
import com.plantilla.offline.application.sync.SyncOperation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ApplicationScoped
public class NotaSyncHandler implements SyncHandler {

    @PersistenceContext(unitName = "plantillaPU")
    private EntityManager em;

    @Override
    public String aggregateType() {
        return "nota";
    }

    /** Subida (lado nube): operación enviada por un servidor local. */
    @Override
    public void apply(SyncOperation op) {
        if ("DELETE".equals(op.operation())) {
            NotaEntity existing = em.find(NotaEntity.class, op.aggregateId());
            if (existing != null && !existing.isDeleted()) {
                existing.markDeleted();
            }
            return;
        }

        NotaEntity entity = em.find(NotaEntity.class, op.aggregateId());
        boolean isNew = entity == null;
        if (isNew) {
            entity = new NotaEntity();
            entity.setId(op.aggregateId());
        }
        entity.setTitulo((String) op.payload().get("titulo"));
        entity.setContenido((String) op.payload().get("contenido"));
        if (isNew) {
            em.persist(entity);
        }
    }

    @Override
    public List<SyncChange> changesSince(long version, int limit) {
        List<NotaEntity> rows = em.createQuery(
                        "select e from NotaEntity e where e.syncVersion > :v order by e.syncVersion",
                        NotaEntity.class)
                .setParameter("v", version)
                .setMaxResults(limit)
                .getResultList();
        return rows.stream().map(e -> {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("titulo", e.getTitulo());
            data.put("contenido", e.getContenido());
            return new SyncChange("nota", e.getId(), e.getSyncVersion(), e.isDeleted(), data);
        }).toList();
    }

    /** Bajada (lado local): cambio recibido de la nube. No escribe en el outbox. */
    @Override
    public void applyChange(SyncChange change) {
        NotaEntity entity = em.find(NotaEntity.class, change.aggregateId());

        if (change.deleted()) {
            if (entity != null && !entity.isDeleted()) {
                entity.markDeleted();
            }
            return;
        }

        String titulo = (String) change.data().get("titulo");
        String contenido = (String) change.data().get("contenido");

        if (entity == null) {
            entity = new NotaEntity();
            entity.setId(change.aggregateId());
            entity.setTitulo(titulo);
            entity.setContenido(contenido);
            em.persist(entity);
            return;
        }
        if (entity.isDeleted()) {
            return;
        }
        // Solo se modifica si algo cambió: así no se dispara el trigger sin necesidad.
        if (!Objects.equals(entity.getTitulo(), titulo)) {
            entity.setTitulo(titulo);
        }
        if (!Objects.equals(entity.getContenido(), contenido)) {
            entity.setContenido(contenido);
        }
    }
}