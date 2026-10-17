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

interface QueueSchema extends DBSchema {
    operations: {
        key: string
        value: QueuedOperation
        indexes: { 'by-created': number }
    }
}

const dbPromise = openDB<QueueSchema>('plantilla-offline', 1, {
    upgrade(db) {
        const store = db.createObjectStore('operations', { keyPath: 'id' })
        store.createIndex('by-created', 'createdAt')
    },
})

export const queueDb = {
    async add(op: QueuedOperation) {
        await (await dbPromise).put('operations', op)
    },
    /** En orden de creación: el orden importa (crear antes de editar). */
    async all(): Promise<QueuedOperation[]> {
        return (await dbPromise).getAllFromIndex('operations', 'by-created')
    },
    async remove(id: string) {
        await (await dbPromise).delete('operations', id)
    },
    async update(op: QueuedOperation) {
        await (await dbPromise).put('operations', op)
    },
    async count(): Promise<number> {
        return (await dbPromise).count('operations')
    },
}