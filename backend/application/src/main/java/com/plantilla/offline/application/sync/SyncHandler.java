package com.plantilla.offline.application.sync;

import java.util.List;

/** Un manejador por tipo de dato: sabe recibir operaciones, publicar cambios y aplicar cambios remotos. */
public interface SyncHandler {
    String aggregateType();

    /** Subida: aplica una operación enviada por un servidor local (lado nube). */
    void apply(SyncOperation operation);

    /** Bajada, lado nube: filas con sync_version mayor a la indicada, en orden. */
    List<SyncChange> changesSince(long version, int limit);

    /** Bajada, lado local: aplica un cambio recibido de la nube. No debe escribir en el outbox. */
    void applyChange(SyncChange change);
}