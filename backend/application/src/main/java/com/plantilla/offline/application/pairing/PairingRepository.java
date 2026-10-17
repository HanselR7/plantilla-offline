package com.plantilla.offline.application.pairing;

import java.time.Instant;
import java.util.UUID;

public interface PairingRepository {
    void saveCode(String codeHash, Instant expiresAt);

    /** Marca el código como usado solo si existe, no está usado y no ha caducado. true si lo logró. */
    boolean consumeCode(String codeHash, Instant now);

    void saveDevice(UUID id, String name, String tokenHash);

    String cloudUrl();
}