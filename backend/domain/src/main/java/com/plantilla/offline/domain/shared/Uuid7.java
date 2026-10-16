package com.plantilla.offline.domain.shared;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

/** UUID v7: ordenable por tiempo, seguro de generar en cualquier dispositivo sin coordinarse. */
public final class Uuid7 {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Uuid7() {
    }

    public static UUID next() {
        long timestamp = System.currentTimeMillis();
        byte[] r = new byte[10];
        RANDOM.nextBytes(r);

        long randA = ((r[0] & 0x0FL) << 8) | (r[1] & 0xFFL);
        long msb = (timestamp << 16) | 0x7000L | randA;

        long randB = ByteBuffer.wrap(r, 2, 8).getLong();
        long lsb = (randB & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;

        return new UUID(msb, lsb);
    }
}