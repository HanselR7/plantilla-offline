package com.plantilla.offline.application.nota;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.plantilla.offline.domain.nota.Nota;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotaServiceTest {

    private NotaRepository repo;
    private NotaService service;

    @BeforeEach
    void setUp() {
        repo = mock(NotaRepository.class);
        service = new NotaService(repo);
    }

    @Test
    void elTituloEsObligatorio() {
        assertThrows(IllegalArgumentException.class, () -> service.crear(null, "  ", "x"));
        verify(repo, never()).save(any());
    }

    @Test
    void creaConElTituloRecortado() {
        when(repo.findById(any())).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Nota nota = service.crear(UUID.randomUUID(), "  Hola  ", "x");

        assertEquals("Hola", nota.getTitulo());
    }

    @Test
    void reenviarLaMismaCreacionNoDuplica() {
        UUID id = UUID.randomUUID();
        Nota existente = new Nota(id, "Ya existe", "x");
        when(repo.findById(id)).thenReturn(Optional.of(existente));

        Nota resultado = service.crear(id, "Otro titulo", "y");

        assertEquals("Ya existe", resultado.getTitulo());
        verify(repo, never()).save(any());
    }

    @Test
    void actualizarUnaNotaInexistenteDevuelveVacio() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertEquals(Optional.empty(), service.actualizar(id, "t", "c"));
    }
}