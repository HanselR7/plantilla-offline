package com.plantilla.offline.application.auth;

import java.util.Optional;

public interface AuthUseCase {
    Optional<AuthTokens> login(String username, String password);

    Optional<AuthTokens> refresh(String refreshToken);

    Optional<AuthUser> authenticate(String accessToken);

    void logout(String accessToken);

    /** Crea el administrador inicial si no hay usuarios. Devuelve la clave solo si la generó. */
    Optional<String> ensureAdmin(String configuredPassword);

    AuthUser createUser(String username, String password, String role);
}