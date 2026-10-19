package com.plantilla.offline.application.sync;

import java.util.List;

public record SyncChangesPage(List<SyncChange> changes, long nextVersion, boolean hasMore) {
}