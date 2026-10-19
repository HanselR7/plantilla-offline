package com.plantilla.offline.application.sync;

/** Aplica operaciones de un tipo de dato (por ejemplo "nota") recibidas de otro nodo. */
public interface SyncHandler {
    String aggregateType();

    void apply(SyncOperation operation);
}