package com.plantilla.offline.application.sync;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SyncApplyService implements SyncApplyUseCase {

    private final Map<String, SyncHandler> handlers;
    private final SyncStateRepository state;
    private final PendingChangesPort pending;

    public SyncApplyService(List<SyncHandler> handlers, SyncStateRepository state, PendingChangesPort pending) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(SyncHandler::aggregateType, Function.identity()));
        this.state = state;
        this.pending = pending;
    }

    @Override
    public List<String> types() {
        return List.copyOf(handlers.keySet());
    }

    @Override
    public long lastVersion(String aggregateType) {
        return state.lastVersion(aggregateType);
    }

    @Override
    public void applyPage(String aggregateType, List<SyncChange> changes, long nextVersion) {
        SyncHandler handler = handlers.get(aggregateType);
        if (handler == null) {
            return;
        }
        for (SyncChange change : changes) {
            // Política: lo pendiente en este servidor gana; se enviará y prevalecerá en la nube.
            if (!pending.hasPending(change.aggregateId())) {
                handler.applyChange(change);
            }
        }
        state.saveVersion(aggregateType, nextVersion);
    }
}