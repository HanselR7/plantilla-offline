import { authFetch } from '../auth/authFetch'
import { useConnectionStore } from '../connectivity/useConnectionStore'
import { queueDb } from './queueDb'
import { useQueueStore } from './useQueueStore'

let running = false

/** Envía la cola en orden. Se detiene ante un fallo recuperable para conservar el orden. */
export async function processQueue(): Promise<void> {
    if (running) return
    const { apiUrl } = useConnectionStore.getState()
    if (!apiUrl) return

    running = true
    useQueueStore.getState().setSyncing(true)
    try {
        for (const op of await queueDb.all()) {
            try {
                const res = await authFetch(`${apiUrl}${op.path}`, {
                    method: op.method,
                    headers: { 'Content-Type': 'application/json', 'Idempotency-Key': op.id },
                    body: op.body,
                })
                if (res.ok || res.status === 404) {
                    // 404 al borrar o editar algo ya inexistente: se da por resuelto.
                    await queueDb.remove(op.id)
                } else if (res.status === 401) {
                    break // sin sesión válida: se conserva todo hasta que el usuario inicie sesión
                } else if (res.status >= 400 && res.status < 500) {
                    await queueDb.remove(op.id) // rechazada por datos inválidos: no sirve reintentarla
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