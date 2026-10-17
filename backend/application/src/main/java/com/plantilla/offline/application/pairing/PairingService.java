package com.plantilla.offline.application.pairing;

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

public class PairingService implements PairingUseCase {

    // Sin caracteres ambiguos (0/O, 1/I) para poder teclearlo sin errores.
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PairingRepository repository;

    public PairingService(PairingRepository repository) {
        this.repository = repository;
    }

    @Override
    public NewPairingCode createCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        String code = sb.toString();
        Instant expiresAt = Instant.now().plus(CODE_TTL);
        repository.saveCode(sha256(code), expiresAt);
        return new NewPairingCode(code, expiresAt);
    }

    @Override
    public Optional<PairingPayload> redeem(String code, String deviceName) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase();
        if (!repository.consumeCode(sha256(normalized), Instant.now())) {
            return Optional.empty();
        }
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String token = HexFormat.of().formatHex(raw);
        UUID deviceId = Uuid7.next();
        String name = (deviceName == null || deviceName.isBlank()) ? "Dispositivo" : deviceName.trim();
        repository.saveDevice(deviceId, name, sha256(token));
        return Optional.of(new PairingPayload(deviceId, token, repository.cloudUrl()));
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