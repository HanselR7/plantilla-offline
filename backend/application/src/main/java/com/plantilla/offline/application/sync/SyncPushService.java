package com.plantilla.offline.application.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SyncPushService implements SyncPushUseCase {

    private final Map<String, SyncHandler> handlers;
    private final ProcessedOperationRepository processed;

    public SyncPushService(List<SyncHandler> handlers, ProcessedOperationRepository processed) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(SyncHandler::aggregateType, Function.identity()));
        this.processed = processed;
    }

    @Override
    public List<UUID> push(SyncBatch batch) {
        List<UUID> confirmed = new ArrayList<>();
        for (SyncOperation op : batch.operations()) {
            SyncHandler handler = handlers.get(op.aggregateType());
            if (handler == null) {
                continue; // tipo desconocido: no se confirma, quedará pendiente en el local
            }
            // Si ya se procesó, se confirma sin volver a aplicar.
            if (processed.markProcessed(op.idempotencyKey())) {
                handler.apply(op);
            }
            confirmed.add(op.idempotencyKey());
        }
        return confirmed;
    }
}