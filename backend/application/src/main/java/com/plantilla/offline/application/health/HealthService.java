package com.plantilla.offline.application.health;

import java.time.Instant;

public class HealthService implements HealthUseCase {

    private final String version;

    public HealthService(String version) {
        this.version = version;
    }

    @Override
    public HealthStatus check() {
        return new HealthStatus("UP", version, Instant.now());
    }
}
