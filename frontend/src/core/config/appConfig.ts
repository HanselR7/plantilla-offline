export const appConfig = {
    /** URL del servidor local guardada tras el descubrimiento o emparejamiento. */
    localServerStorageKey: 'plantilla.localServerUrl',
    /** URL de la API en la nube. TODO: moverla a una variable de entorno. */
    cloudApiUrl: 'https://api.ejemplo.com/plantilla-api/api',
    /** Servidor local por defecto en desarrollo. */
    defaultLocalUrl: 'http://localhost:8080/plantilla-api/api',
    healthTimeoutMs: 2500,
    checkIntervalMs: 15000,
}