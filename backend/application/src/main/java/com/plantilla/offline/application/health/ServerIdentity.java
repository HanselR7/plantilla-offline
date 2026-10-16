package com.plantilla.offline.application.health;

import java.util.UUID;

public record ServerIdentity(UUID installationId, String name, String mode) {
}