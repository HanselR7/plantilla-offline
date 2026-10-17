import { useConnectionStore } from '../connectivity/useConnectionStore'
import { queueDb } from './queueDb'
import { useQueueStore } from './useQueueStore'

let running = false

/** Envía la cola en orden. Se detiene al primer fallo de red para conservar el orden. */
export async function processQueue(): Promise<void> {
    if (running) return
    const { apiUrl } = useConnectionStore.getState()
    if (!apiUrl) return

    running = true
    useQueueStore.getState().setSyncing(true)
    try {
        for (const op of await queueDb.all()) {
            try {
                const res = await fetch(`${apiUrl}${op.path}`, {
                    method: op.method,
                    headers: { 'Content-Type': 'application/json', 'Idempotency-Key': op.id },
                    body: op.body,
                })
                // 404 al borrar o editar algo ya inexistente: se da por resuelto.
                if (res.ok || res.status === 404) {
                    await queueDb.remove(op.id)
                } else if (res.status >= 400 && res.status < 500) {
                    // Rechazada por el servidor (datos inválidos): no sirve reintentarla.
                    await queueDb.update({ ...op, attempts: op.attempts + 1, lastError: `HTTP ${res.status}` })
                    await queueDb.remove(op.id)
                } else {
                    break // error del servidor: reintentar luego
                }
            } catch {
                break // sin red: reintentar luego
            }
        }
    } finally {
        running = false
        useQueueStore.getState().setSyncing(false)
        await useQueueStore.getState().refreshCount()
    }
}