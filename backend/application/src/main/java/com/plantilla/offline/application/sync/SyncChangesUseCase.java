package com.plantilla.offline.application.sync;

import java.util.Optional;

public interface SyncChangesUseCase {
    /** Vacío si el tipo no existe. */
    Optional<SyncChangesPage> changes(String aggregateType, long since, int limit);
}