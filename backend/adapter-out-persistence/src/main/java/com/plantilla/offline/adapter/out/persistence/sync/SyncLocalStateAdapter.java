package com.plantilla.offline.adapter.out.persistence.sync;

import com.plantilla.offline.application.sync.PendingChangesPort;
import com.plantilla.offline.application.sync.SyncStateRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SyncLocalStateAdapter implements SyncStateRepository, PendingChangesPort {

    @PersistenceContext(unitName = "plantillaPU")
    private EntityManager em;

    @Override
    public long lastVersion(String aggregateType) {
        List<?> rows = em.createNativeQuery("select last_version from sync_state where entity_type = :t")
                .setParameter("t", aggregateType)
                .getResultList();
        return rows.isEmpty() ? 0L : ((Number) rows.get(0)).longValue();
    }

    @Override
    public void saveVersion(String aggregateType, long version) {
        em.createNativeQuery(
                        "insert into sync_state (entity_type, last_version, last_synced_at) values (:t, :v, now()) "
                                + "on conflict (entity_type) do update "
                                + "set last_version = excluded.last_version, last_synced_at = now()")
                .setParameter("t", aggregateType)
                .setParameter("v", version)
                .executeUpdate();
    }

    @Override
    public boolean hasPending(UUID aggregateId) {
        Object count = em.createNativeQuery(
                        "select count(*) from outbox where aggregate_id = :id and sent_at is null")
                .setParameter("id", aggregateId)
                .getSingleResult();
        return ((Number) count).longValue() > 0;
    }
}