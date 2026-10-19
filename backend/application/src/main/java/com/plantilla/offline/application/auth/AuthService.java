package com.plantilla.offline.application.auth;

import com.plantilla.offline.domain.shared.Uuid7;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class AuthService implements AuthUseCase {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    private static final Duration ACCESS_TTL = Duration.ofMinutes(15);
    private static final Duration REFRESH_TTL = Duration.ofDays(30);
    private static final int MIN_PASSWORD = 8;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,64}$");
    // Se verifica contra este hash cuando el usuario no existe, para que el tiempo de respuesta
    // no revele si el usuario existe.
    private static final String DUMMY_HASH = PasswordHasher.hash("contrasena-ficticia");

    private final AuthRepository repository;

    public AuthService(AuthRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AuthTokens> login(String username, String password) {
        String name = username == null ? "" : username.trim();
        Optional<StoredUser> user = repository.findUser(name);
        String hash = user.map(StoredUser::passwordHash).orElse(DUMMY_HASH);
        boolean valid = PasswordHasher.verify(password == null ? "" : password, hash);
        if (!valid || user.isEmpty() || user.get().disabled()) {
            return Optional.empty();
        }
        StoredUser u = user.get();
        String access = randomToken();
        String refresh = randomToken();
        Instant now = Instant.now();
        Instant accessExp = now.plus(ACCESS_TTL);
        Instant refreshExp = now.plus(REFRESH_TTL);
        repository.createSession(Uuid7.next(), u.id(), sha256(access), accessExp, sha256(refresh), refreshExp);
        return Optional.of(new AuthTokens(access, accessExp, refresh, refreshExp,
                new AuthUser(u.id(), u.username(), u.role())));
    }

    @Override
    public Optional<AuthTokens> refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return Optional.empty();
        }
        String access = randomToken();
        String refresh = randomToken();
        Instant now = Instant.now();
        Instant accessExp = now.plus(ACCESS_TTL);
        Instant refreshExp = now.plus(REFRESH_TTL);
        return repository
                .rotateSession(sha256(refreshToken.trim()), now, sha256(access), accessExp, sha256(refresh), refreshExp)
                .map(user -> new AuthTokens(access, accessExp, refresh, refreshExp, user));
    }

    @Override
    public Optional<AuthUser> authenticate(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return Optional.empty();
        }
        return repository.findUserByAccessHash(sha256(accessToken), Instant.now());
    }

    @Override
    public void logout(String accessToken) {
        if (accessToken != null && !accessToken.isBlank()) {
            repository.revokeByAccessHash(sha256(accessToken));
        }
    }

    @Override
    public Optional<String> ensureAdmin(String configuredPassword) {
        if (repository.countUsers() > 0) {
            return Optional.empty();
        }
        boolean generated = configuredPassword == null || configuredPassword.isBlank();
        String password = generated ? randomToken().substring(0, 16) : configuredPassword;
        if (password.length() < MIN_PASSWORD) {
            throw new IllegalStateException("La contraseña del administrador debe tener al menos 8 caracteres");
        }
        repository.createUser(Uuid7.next(), "admin", PasswordHasher.hash(password), ROLE_ADMIN);
        return generated ? Optional.of(password) : Optional.empty();
    }

    @Override
    public AuthUser createUser(String username, String password, String role) {
        String name = username == null ? "" : username.trim();
        if (!USERNAME.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "El usuario debe tener de 3 a 64 caracteres: letras, números, punto, guion o guion bajo");
        }
        if (password == null || password.length() < MIN_PASSWORD || password.length() > 128) {
            throw new IllegalArgumentException("La contraseña debe tener entre 8 y 128 caracteres");
        }
        String r = role == null ? ROLE_USER : role.trim().toUpperCase();
        if (!r.equals(ROLE_ADMIN) && !r.equals(ROLE_USER)) {
            throw new IllegalArgumentException("Rol inválido (use ADMIN o USER)");
        }
        if (repository.findUser(name).isPresent()) {
            throw new IllegalArgumentException("Ese usuario ya existe");
        }
        UUID id = Uuid7.next();
        repository.createUser(id, name, PasswordHasher.hash(password), r);
        return new AuthUser(id, name, r);
    }

    private static String randomToken() {
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        return HexFormat.of().formatHex(raw);
    }

    private static String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}