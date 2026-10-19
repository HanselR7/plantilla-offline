package com.plantilla.offline.application.sync;

public interface SyncStateRepository {
    long lastVersion(String aggregateType);

    void saveVersion(String aggregateType, long version);
}