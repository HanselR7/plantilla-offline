package com.plantilla.offline.application.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SyncPushServiceTest {

    private SyncHandler handler;
    private ProcessedOperationRepository processed;
    private SyncPushService service;

    @BeforeEach
    void setUp() {
        handler = mock(SyncHandler.class);
        when(handler.aggregateType()).thenReturn("nota");
        processed = mock(ProcessedOperationRepository.class);
        service = new SyncPushService(List.of(handler), processed);
    }

    private SyncOperation op(UUID key, String type) {
        return new SyncOperation(key, type, UUID.randomUUID(), "UPSERT", Map.of("titulo", "x"));
    }

    @Test
    void aplicaUnaOperacionNueva() {
        UUID key = UUID.randomUUID();
        when(processed.markProcessed(key)).thenReturn(true);

        List<UUID> confirmed = service.push(new SyncBatch(UUID.randomUUID(), List.of(op(key, "nota"))));

        assertEquals(List.of(key), confirmed);
        verify(handler, times(1)).apply(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void unaOperacionRepetidaSeConfirmaPeroNoSeAplica() {
        UUID key = UUID.randomUUID();
        when(processed.markProcessed(key)).thenReturn(false);

        List<UUID> confirmed = service.push(new SyncBatch(UUID.randomUUID(), List.of(op(key, "nota"))));

        assertEquals(List.of(key), confirmed);
        verify(handler, never()).apply(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void unTipoDesconocidoNoSeConfirma() {
        UUID key = UUID.randomUUID();

        List<UUID> confirmed = service.push(new SyncBatch(UUID.randomUUID(), List.of(op(key, "desconocido"))));

        assertTrue(confirmed.isEmpty());
        verify(processed, never()).markProcessed(org.mockito.ArgumentMatchers.any());
    }
}