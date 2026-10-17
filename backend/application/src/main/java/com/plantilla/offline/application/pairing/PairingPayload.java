package com.plantilla.offline.application.pairing;

import java.util.UUID;

public record PairingPayload(UUID deviceId, String deviceToken, String cloudUrl) {
}