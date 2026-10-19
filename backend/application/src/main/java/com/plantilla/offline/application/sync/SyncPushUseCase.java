package com.plantilla.offline.application.sync;

import java.util.List;
import java.util.UUID;

public interface SyncPushUseCase {
    /** Aplica el lote y devuelve las claves confirmadas (nuevas o ya procesadas). */
    List<UUID> push(SyncBatch batch);
}