export type ConnectionMode = 'checking' | 'local' | 'cloud' | 'offline'

export interface ServerHealth {
    status: string
    database: boolean
    server?: { installationId: string; name: string; mode: string }
}

export interface ConnectionState {
    mode: ConnectionMode
    /** URL base de la API activa (null si estamos offline). */
    apiUrl: string | null
    server?: ServerHealth['server']
}