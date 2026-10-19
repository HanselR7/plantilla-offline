package com.plantilla.offline.adapter.out.persistence.nota;

import com.plantilla.offline.application.sync.SyncHandler;
import com.plantilla.offline.application.sync.SyncOperation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class NotaSyncHandler implements SyncHandler {

    @PersistenceContext(unitName = "plantillaPU")
    private EntityManager em;

    @Override
    public String aggregateType() {
        return "nota";
    }

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
}