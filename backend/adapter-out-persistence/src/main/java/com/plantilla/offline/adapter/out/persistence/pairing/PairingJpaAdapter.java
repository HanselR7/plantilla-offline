package com.plantilla.offline.adapter.out.persistence.pairing;

import com.plantilla.offline.application.pairing.PairingRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
@Transactional
public class PairingJpaAdapter implements PairingRepository {

    @PersistenceContext(unitName = "plantillaPU")
    private EntityManager em;

    @Override
    public void saveCode(String codeHash, Instant expiresAt) {
        em.createNativeQuery("insert into pairing_code (code_hash, expires_at) values (:h, :e)")
                .setParameter("h", codeHash)
                .setParameter("e", expiresAt)
                .executeUpdate();
    }

    @Override
    public boolean consumeCode(String codeHash, Instant now) {
        // Un solo UPDATE condicional: si dos dispositivos canjean a la vez, solo uno gana.
        int rows = em.createNativeQuery(
                        "update pairing_code set used_at = :now "
                                + "where code_hash = :h and used_at is null and expires_at > :now")
                .setParameter("now", now)
                .setParameter("h", codeHash)
                .executeUpdate();
        return rows == 1;
    }

    @Override
    public void saveDevice(UUID id, String name, String tokenHash) {
        em.createNativeQuery("insert into paired_device (id, name, token_hash) values (:id, :n, :t)")
                .setParameter("id", id)
                .setParameter("n", name)
                .setParameter("t", tokenHash)
                .executeUpdate();
    }

    @Override
    public String cloudUrl() {
        Object value = em.createNativeQuery("select cloud_url from installation limit 1").getSingleResult();
        return value == null ? null : value.toString();
    }
}