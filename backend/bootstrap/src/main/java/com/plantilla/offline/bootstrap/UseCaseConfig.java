package com.plantilla.offline.bootstrap;

import com.plantilla.offline.application.health.HealthService;
import com.plantilla.offline.application.health.HealthUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class UseCaseConfig {

    @Produces
    @ApplicationScoped
    public HealthUseCase healthUseCase() {
        return new HealthService("1.0.0-SNAPSHOT");
    }
}
