package com.plantilla.offline.adapter.out.persistence.base;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // Los asigna el trigger sync_touch() de la base de datos: solo lectura para JPA.
    @Column(name = "sync_version", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private Long syncVersion;

    @Column(name = "updated_at", insertable = false, updatable = false)
    @Setter(lombok.AccessLevel.NONE)
    private Instant updatedAt;

    // Borrado lógico: el registro no se elimina, para que el borrado también viaje a la nube.
    @Column(name = "deleted_at")
    @Setter(lombok.AccessLevel.NONE)
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markDeleted() {
        this.deletedAt = Instant.now();
    }
}