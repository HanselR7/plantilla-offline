package com.plantilla.offline.application.sync;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SyncApplyServiceTest {

    private SyncHandler handler;
    private SyncStateRepository state;
    private PendingChangesPort pending;
    private SyncApplyService service;

    @BeforeEach
    void setUp() {
        handler = mock(SyncHandler.class);
        when(handler.aggregateType()).thenReturn("nota");
        state = mock(SyncStateRepository.class);
        pending = mock(PendingChangesPort.class);
        service = new SyncApplyService(List.of(handler), state, pending);
    }

    private SyncChange change(UUID id, long version) {
        return new SyncChange("nota", id, version, false, Map.of("titulo", "x"));
    }

    @Test
    void aplicaLosCambiosYAvanzaElCursor() {
        UUID id = UUID.randomUUID();
        when(pending.hasPending(id)).thenReturn(false);

        service.applyPage("nota", List.of(change(id, 5)), 5);

        verify(handler).applyChange(any());
        verify(state).saveVersion("nota", 5);
    }

    @Test
    void loPendienteLocalNoSePisa() {
        UUID id = UUID.randomUUID();
        when(pending.hasPending(id)).thenReturn(true);

        service.applyPage("nota", List.of(change(id, 7)), 7);

        verify(handler, never()).applyChange(any());
        // El cursor avanza igual: el cambio remoto se descarta a propósito, no se reintenta.
        verify(state).saveVersion("nota", 7);
    }

    @Test
    void unTipoDesconocidoNoTocaElCursor() {
        service.applyPage("desconocido", List.of(), 9);

        verify(state, never()).saveVersion(any(), org.mockito.ArgumentMatchers.anyLong());
    }
}