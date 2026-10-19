package com.plantilla.offline.application.sync;

import java.util.UUID;

public interface ProcessedOperationRepository {
    /** Registra la clave. Devuelve false si ya existía (operación repetida). */
    boolean markProcessed(UUID idempotencyKey);
}