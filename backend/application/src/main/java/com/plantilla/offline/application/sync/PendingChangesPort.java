package com.plantilla.offline.application.sync;

import java.util.UUID;

public interface PendingChangesPort {
    /** true si ese registro tiene cambios locales todavía sin enviar a la nube. */
    boolean hasPending(UUID aggregateId);
}