package com.plantilla.offline.application.pairing;

import java.time.Instant;

public record NewPairingCode(String code, Instant expiresAt) {
}