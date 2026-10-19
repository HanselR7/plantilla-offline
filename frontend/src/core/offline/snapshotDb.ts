import { dbPromise } from './db'

export const snapshotDb = {
    async put(key: string, data: unknown) {
        await (await dbPromise).put('snapshots', { key, data, savedAt: Date.now() })
    },
    async get<T>(key: string): Promise<T | null> {
        const row = await (await dbPromise).get('snapshots', key)
        return row ? (row.data as T) : null
    },
}