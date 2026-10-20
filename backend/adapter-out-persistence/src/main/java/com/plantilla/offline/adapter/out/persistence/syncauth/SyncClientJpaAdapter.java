package com.plantilla.offline.adapter.out.persistence.syncauth;

import com.plantilla.offline.application.syncauth.SyncClientRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.UUID;

@ApplicationScoped
@Transactional
public class SyncClientJpaAdapter implements SyncClientRepository {

    @PersistenceContext(unitName = "plantillaPU")
    private EntityManager em;

    @Override
    public void upsertClient(UUID id, String name, String tokenHash) {
        em.createNativeQuery(
                        "insert into sync_client (id, name, token_hash) values (:id, :n, :t) "
                                + "on conflict (id) do update set name = excluded.name, "
                                + "token_hash = excluded.token_hash, revoked_at = null")
                .setParameter("id", id)
                .setParameter("n", name)
                .setParameter("t", tokenHash)
                .executeUpdate();
    }

    @Override
    public boolean validate(UUID id, String tokenHash) {
        int rows = em.createNativeQuery(
                        "update sync_client set last_seen_at = now() "
                                + "where id = :id and token_hash = :t and revoked_at is null")
                .setParameter("id", id)
                .setParameter("t", tokenHash)
                .executeUpdate();
        return rows == 1;
    }

    @Override
    public boolean revoke(UUID id) {
        return em.createNativeQuery(
                        "update sync_client set revoked_at = now() where id = :id and revoked_at is null")
                .setParameter("id", id)
                .executeUpdate() == 1;
    }
}