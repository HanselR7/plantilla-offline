package com.plantilla.offline.application.pairing;

import java.util.Optional;

public interface PairingUseCase {
    NewPairingCode createCode();

    Optional<PairingPayload> redeem(String code, String deviceName);
}