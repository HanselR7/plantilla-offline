import { v7 as uuidv7 } from 'uuid'
import { useConnectionStore } from '../connectivity/useConnectionStore'
import { queueDb } from '../offline/queueDb'
import { useQueueStore } from '../offline/useQueueStore'

export class OfflineError extends Error {
    constructor() {
        super('Sin conexión con el servidor')
        this.name = 'OfflineError'
    }
}

/** Resultado de una escritura que quedó en cola en vez de enviarse. */
export interface QueuedResult {
    queued: true
}

export async function http<T>(path: string, init?: RequestInit): Promise<T> {
    const { apiUrl } = useConnectionStore.getState()
    if (!apiUrl) throw new OfflineError()

    const res = await fetch(`${apiUrl}${path}`, {
        ...init,
        headers: { 'Content-Type': 'application/json', ...init?.headers },
    })

    if (!res.ok) {
        const message = await res.text()
        throw new Error(message || `Error ${res.status}`)
    }
    return res.status === 204 ? (undefined as T) : ((await res.json()) as T)
}

/** Escritura con respaldo offline: si no hay servidor, se guarda en la cola. */
export async function write<T>(
    method: 'POST' | 'PUT' | 'DELETE',
    path: string,
    body?: unknown,
): Promise<T | QueuedResult> {
    const idempotencyKey = uuidv7()
    const payload = body === undefined ? null : JSON.stringify(body)

    const { apiUrl } = useConnectionStore.getState()
    if (apiUrl) {
        try {
            const res = await fetch(`${apiUrl}${path}`, {
                method,
                headers: { 'Content-Type': 'application/json', 'Idempotency-Key': idempotencyKey },
                body: payload,
            })
            if (!res.ok) {
                throw new Error((await res.text()) || `Error ${res.status}`)
            }
            return res.status === 204 ? (undefined as T) : ((await res.json()) as T)
        } catch (e) {
            // Error de negocio del servidor: se muestra. Error de red: se encola.
            if (!(e instanceof TypeError)) throw e
        }
    }

    await queueDb.add({
        id: idempotencyKey,
        method,
        path,
        body: payload,
        createdAt: Date.now(),
        attempts: 0,
    })
    await useQueueStore.getState().refreshCount()
    return { queued: true }
}

export function isQueued(result: unknown): result is QueuedResult {
    return typeof result === 'object' && result !== null && 'queued' in result
}