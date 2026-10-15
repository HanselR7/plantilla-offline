package com.plantilla.offline.application.health;

/** Puerto de entrada: comprobar el estado del sistema. */
public interface HealthUseCase {
    HealthStatus check();
}
