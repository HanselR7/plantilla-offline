package com.plantilla.offline.application.sync;

import java.util.Map;
import java.util.UUID;

public record SyncChange(String aggregateType, UUID aggregateId, long version,
                         boolean deleted, Map<String, Object> data) {
}