package com.plantilla.offline.adapter.out.persistence.base;

import com.plantilla.offline.domain.shared.Uuid7;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Operaciones comunes sobre entidades. Los repositorios de cada modelo la usan internamente. */
public class JpaRepositoryBase<T extends BaseEntity> {

    private final EntityManager em;
    private final Class<T> type;

    public JpaRepositoryBase(EntityManager em, Class<T> type) {
        this.em = em;
        this.type = type;
    }

    public T saveEntity(T entity) {
        if (entity.getId() == null) {
            entity.setId(Uuid7.next());
            em.persist(entity);
            return entity;
        }
        return em.merge(entity);
    }

    public Optional<T> findEntity(UUID id) {
        return Optional.ofNullable(em.find(type, id)).filter(e -> !e.isDeleted());
    }

    public List<T> findAllEntities() {
        return em.createQuery(
                        "select e from " + type.getSimpleName() + " e where e.deletedAt is null", type)
                .getResultList();
    }

    public boolean softDeleteEntity(UUID id) {
        T entity = em.find(type, id);
        if (entity == null || entity.isDeleted()) {
            return false;
        }
        entity.markDeleted();
        return true;
    }
}