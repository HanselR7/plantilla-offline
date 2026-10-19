import { dbPromise } from './db'
import type { QueuedOperation } from './db'

export type { QueuedOperation }

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