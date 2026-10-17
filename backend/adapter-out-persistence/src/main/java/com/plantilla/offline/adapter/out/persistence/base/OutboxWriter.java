package com.plantilla.offline.adapter.out.persistence.base;

import com.plantilla.offline.domain.shared.Uuid7;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Map;
import java.util.UUID;

/** Registra cambios en la tabla outbox, dentro de la transacción del llamador. */
@ApplicationScoped
public class OutboxWriter {

    @PersistenceContext(unitName = "plantillaPU")
    private EntityManager em;

    private final Jsonb jsonb = JsonbBuilder.create();

    private final boolean enabled = "LOCAL".equalsIgnoreCase(
            System.getProperty("plantilla.mode", System.getenv().getOrDefault("PLANTILLA_MODE", "LOCAL")).trim());

    public void record(String aggregateType, UUID aggregateId, String operation, Map<String, Object> payload) {
        if (!enabled) {
            return;
        }
        em.createNativeQuery(
                        "insert into outbox (id, aggregate_type, aggregate_id, operation, payload, idempotency_key) "
                                + "values (:id, :type, :agg, :op, cast(:payload as jsonb), :key)")
                .setParameter("id", Uuid7.next())
                .setParameter("type", aggregateType)
                .setParameter("agg", aggregateId)
                .setParameter("op", operation)
                .setParameter("payload", jsonb.toJson(payload))
                .setParameter("key", Uuid7.next())
                .executeUpdate();
    }
}