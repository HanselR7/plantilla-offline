package com.plantilla.offline.application.auth;

import java.util.UUID;

public record StoredUser(UUID id, String username, String passwordHash, String role, boolean disabled) {
}