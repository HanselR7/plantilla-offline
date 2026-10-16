package com.plantilla.offline.adapter.out.persistence.server;

import com.plantilla.offline.application.health.ServerIdentity;
import com.plantilla.offline.application.health.ServerInfoPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;

@ApplicationScoped
public class ServerInfoJpaAdapter implements ServerInfoPort {

    @PersistenceContext(unitName = "plantillaPU")
    private EntityManager em;

    @Override
    public ServerIdentity identity() {
        Object[] row = (Object[]) em
                .createNativeQuery("select id, name, mode from installation limit 1")
                .getSingleResult();
        return new ServerIdentity((UUID) row[0], (String) row[1], (String) row[2]);
    }

    @Override
    public boolean databaseAvailable() {
        try {
            em.createNativeQuery("select 1").getSingleResult();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}