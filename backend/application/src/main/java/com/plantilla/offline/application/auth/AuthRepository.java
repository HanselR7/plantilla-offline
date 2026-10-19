package com.plantilla.offline.application.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AuthRepository {
    long countUsers();

    Optional<StoredUser> findUser(String username);

    void createUser(UUID id, String username, String passwordHash, String role);

    void createSession(UUID id, UUID userId, String accessHash, Instant accessExpiresAt,
                       String refreshHash, Instant refreshExpiresAt);

    Optional<AuthUser> findUserByAccessHash(String accessHash, Instant now);

    /** Si el refresh es válido, reemplaza ambos tokens (de un solo uso) y devuelve el usuario. */
    Optional<AuthUser> rotateSession(String oldRefreshHash, Instant now, String newAccessHash,
                                     Instant newAccessExpiresAt, String newRefreshHash,
                                     Instant newRefreshExpiresAt);

    void revokeByAccessHash(String accessHash);
}