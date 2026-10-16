package com.plantilla.offline.application.health;

/** Puerto de salida: información del servidor que solo la persistencia conoce. */
public interface ServerInfoPort {
    ServerIdentity identity();
    boolean databaseAvailable();
}