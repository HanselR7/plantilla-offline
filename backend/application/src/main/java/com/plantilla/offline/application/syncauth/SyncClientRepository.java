package com.plantilla.offline.application.syncauth;

import java.util.UUID;

public interface SyncClientRepository {
    /** Crea la instalación o, si ya existía, le rota el token y la reactiva. */
    void upsertClient(UUID id, String name, String tokenHash);

    /** true si el id y el hash coinciden con un cliente no revocado. Actualiza last_seen_at. */
    boolean validate(UUID id, String tokenHash);

    boolean revoke(UUID id);
}