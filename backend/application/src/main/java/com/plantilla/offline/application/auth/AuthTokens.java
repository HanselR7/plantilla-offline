package com.plantilla.offline.application.auth;

import java.time.Instant;

public record AuthTokens(String accessToken, Instant accessExpiresAt,
                         String refreshToken, Instant refreshExpiresAt, AuthUser user) {
}