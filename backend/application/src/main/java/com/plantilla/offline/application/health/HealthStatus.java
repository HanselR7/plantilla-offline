package com.plantilla.offline.application.health;

import java.time.Instant;

public record HealthStatus(String status, String version, Instant timestamp) {
}
