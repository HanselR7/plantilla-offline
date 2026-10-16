package com.plantilla.offline.application.health;

import java.time.Instant;

public class HealthService implements HealthUseCase {

    private final String version;
    private final ServerInfoPort serverInfo;

    public HealthService(String version, ServerInfoPort serverInfo) {
        this.version = version;
        this.serverInfo = serverInfo;
    }

    @Override
    public HealthStatus check() {
        boolean dbUp = serverInfo.databaseAvailable();
        ServerIdentity identity = dbUp ? serverInfo.identity() : null;
        return new HealthStatus(dbUp ? "UP" : "DEGRADED", version, Instant.now(), dbUp, identity);
    }
}