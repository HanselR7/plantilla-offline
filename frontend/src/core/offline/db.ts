import { openDB } from 'idb'
import type { DBSchema } from 'idb'

export interface QueuedOperation {
    /** Clave de idempotencia de la operación. */
    id: string
    method: 'POST' | 'PUT' | 'DELETE'
    path: string
    body: string | null
    createdAt: number
    attempts: number
    lastError?: string
}

export interface Snapshot {
    key: string
    data: unknown
    savedAt: number
}

interface OfflineSchema extends DBSchema {
    operations: {
        key: string
        value: QueuedOperation
        indexes: { 'by-created': number }
    }
    snapshots: {
        key: string
        value: Snapshot
    }
}

export const dbPromise = openDB<OfflineSchema>('plantilla-offline', 2, {
    upgrade(db, oldVersion) {
        if (oldVersion < 1) {
            const store = db.createObjectStore('operations', { keyPath: 'id' })
            store.createIndex('by-created', 'createdAt')
        }
        if (oldVersion < 2) {
            db.createObjectStore('snapshots', { keyPath: 'key' })
        }
    },
})