package com.plantilla.offline.application.sync;

import java.util.List;

public interface SyncApplyUseCase {
    List<String> types();

    long lastVersion(String aggregateType);

    /** Aplica una página de cambios de la nube y avanza el cursor. Debe correr en una transacción. */
    void applyPage(String aggregateType, List<SyncChange> changes, long nextVersion);
}