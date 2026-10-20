package com.plantilla.offline.application.syncauth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.UUID;

public class SyncClientService implements SyncClientUseCase {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SyncClientRepository repository;

    public SyncClientService(SyncClientRepository repository) {
        this.repository = repository;
    }

    @Override
    public String register(UUID installationId, String name) {
        if (installationId == null) {
            throw new IllegalArgumentException("installationId es obligatorio");
        }
        String clean = (name == null || name.isBlank()) ? "Instalación local" : name.trim();
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String token = HexFormat.of().formatHex(raw);
        repository.upsertClient(installationId, clean, sha256(token));
        return token;
    }

    @Override
    public boolean authenticate(UUID installationId, String token) {
        if (installationId == null || token == null || token.isBlank()) {
            return false;
        }
        return repository.validate(installationId, sha256(token.trim()));
    }

    @Override
    public boolean revoke(UUID installationId) {
        return repository.revoke(installationId);
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