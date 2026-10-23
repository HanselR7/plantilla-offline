package com.plantilla.offline.application.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    @Test
    void verificaLaClaveCorrecta() {
        String hash = PasswordHasher.hash("ClaveSegura123");
        assertTrue(PasswordHasher.verify("ClaveSegura123", hash));
    }

    @Test
    void rechazaUnaClaveIncorrecta() {
        String hash = PasswordHasher.hash("ClaveSegura123");
        assertFalse(PasswordHasher.verify("otra-clave", hash));
    }

    @Test
    void dosHashesDeLaMismaClaveSonDistintos() {
        assertNotEquals(PasswordHasher.hash("igual"), PasswordHasher.hash("igual"));
    }

    @Test
    void rechazaUnHashMalFormado() {
        assertFalse(PasswordHasher.verify("x", "esto-no-es-un-hash"));
    }
}