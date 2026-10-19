package com.plantilla.offline.adapter.out.persistence.sync;

import com.plantilla.offline.application.sync.ProcessedOperationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;

@ApplicationScoped
public class ProcessedOperationJpaAdapter implements ProcessedOperationRepository {

    @PersistenceContext(unitName = "plantillaPU")
    private EntityManager em;

    @Override
    public boolean markProcessed(UUID idempotencyKey) {
        int rows = em.createNativeQuery(
                        "insert into processed_operation (idempotency_key) values (:k) "
                                + "on conflict do nothing")
                .setParameter("k", idempotencyKey)
                .executeUpdate();
        return rows == 1;
    }
}