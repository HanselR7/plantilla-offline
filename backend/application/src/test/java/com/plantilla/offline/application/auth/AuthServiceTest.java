package com.plantilla.offline.application.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    private AuthRepository repo;
    private AuthService service;

    @BeforeEach
    void setUp() {
        repo = mock(AuthRepository.class);
        service = new AuthService(repo);
    }

    private StoredUser user(boolean disabled) {
        return new StoredUser(UUID.randomUUID(), "ana", PasswordHasher.hash("ClaveAna1234"), "USER", disabled);
    }

    @Test
    void loginCorrectoCreaSesion() {
        when(repo.findUser("ana")).thenReturn(Optional.of(user(false)));

        Optional<AuthTokens> tokens = service.login("ana", "ClaveAna1234");

        assertTrue(tokens.isPresent());
        verify(repo).createSession(any(), any(), anyString(), any(), anyString(), any());
    }

    @Test
    void loginConClaveIncorrectaFalla() {
        when(repo.findUser("ana")).thenReturn(Optional.of(user(false)));

        assertTrue(service.login("ana", "mala").isEmpty());
        verify(repo, never()).createSession(any(), any(), any(), any(), any(), any());
    }

    @Test
    void loginConUsuarioInexistenteFalla() {
        when(repo.findUser("nadie")).thenReturn(Optional.empty());

        assertTrue(service.login("nadie", "ClaveAna1234").isEmpty());
    }

    @Test
    void usuarioDeshabilitadoNoEntra() {
        when(repo.findUser("ana")).thenReturn(Optional.of(user(true)));

        assertTrue(service.login("ana", "ClaveAna1234").isEmpty());
    }

    @Test
    void crearUsuarioValidaLaClave() {
        assertThrows(IllegalArgumentException.class, () -> service.createUser("ana", "corta", "USER"));
    }

    @Test
    void crearUsuarioValidaElNombre() {
        assertThrows(IllegalArgumentException.class, () -> service.createUser("a b", "ClaveLarga123", "USER"));
    }

    @Test
    void crearUsuarioValidaElRol() {
        assertThrows(IllegalArgumentException.class, () -> service.createUser("ana", "ClaveLarga123", "ROOT"));
    }

    @Test
    void crearUsuarioRechazaDuplicados() {
        when(repo.findUser("ana")).thenReturn(Optional.of(user(false)));

        assertThrows(IllegalArgumentException.class, () -> service.createUser("ana", "ClaveLarga123", "USER"));
    }

    @Test
    void crearUsuarioNormalizaElRol() {
        when(repo.findUser("ana")).thenReturn(Optional.empty());

        AuthUser created = service.createUser("ana", "ClaveLarga123", "user");

        assertEquals("USER", created.role());
    }

    @Test
    void ensureAdminNoHaceNadaSiYaHayUsuarios() {
        when(repo.countUsers()).thenReturn(1L);

        assertTrue(service.ensureAdmin(null).isEmpty());
        verify(repo, never()).createUser(any(), anyString(), anyString(), anyString());
    }

    @Test
    void ensureAdminGeneraClaveSiNoSeConfigura() {
        when(repo.countUsers()).thenReturn(0L);

        Optional<String> generated = service.ensureAdmin(null);

        assertTrue(generated.isPresent());
        verify(repo).createUser(any(), anyString(), anyString(), anyString());
    }
}