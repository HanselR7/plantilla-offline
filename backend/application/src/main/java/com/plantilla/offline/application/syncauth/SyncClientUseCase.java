package com.plantilla.offline.application.syncauth;

import java.util.UUID;

public interface SyncClientUseCase {
    /** Registra la instalación y devuelve su token (única vez que se conoce en claro). */
    String register(UUID installationId, String name);

    boolean authenticate(UUID installationId, String token);

    boolean revoke(UUID installationId);
}