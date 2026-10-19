package com.plantilla.offline.application.sync;

import java.util.List;
import java.util.UUID;

public record SyncBatch(UUID installationId, List<SyncOperation> operations) {
}