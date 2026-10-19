package com.plantilla.offline.application.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SyncChangesService implements SyncChangesUseCase {

    private static final int MAX_LIMIT = 500;

    private final Map<String, SyncHandler> handlers;

    public SyncChangesService(List<SyncHandler> handlers) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(SyncHandler::aggregateType, Function.identity()));
    }

    @Override
    public Optional<SyncChangesPage> changes(String aggregateType, long since, int limit) {
        SyncHandler handler = handlers.get(aggregateType);
        if (handler == null) {
            return Optional.empty();
        }
        int size = Math.min(Math.max(limit, 1), MAX_LIMIT);
        // Se pide uno de más para saber si quedan páginas.
        List<SyncChange> rows = handler.changesSince(since, size + 1);
        boolean more = rows.size() > size;
        List<SyncChange> page = new ArrayList<>(more ? rows.subList(0, size) : rows);
        long next = page.isEmpty() ? since : page.get(page.size() - 1).version();
        return Optional.of(new SyncChangesPage(page, next, more));
    }
}