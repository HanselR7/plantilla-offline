package com.plantilla.offline.application.sync;

import java.util.Map;
import java.util.UUID;

public record SyncOperation(UUID idempotencyKey, String aggregateType, UUID aggregateId,
                            String operation, Map<String, Object> payload) {
}