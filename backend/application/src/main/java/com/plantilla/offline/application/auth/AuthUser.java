package com.plantilla.offline.application.auth;

import java.util.UUID;

public record AuthUser(UUID id, String username, String role) {
}