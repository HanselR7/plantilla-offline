import { useConnectionStore } from '../connectivity/useConnectionStore'

export class OfflineError extends Error {
    constructor() {
        super('Sin conexión con el servidor')
        this.name = 'OfflineError'
    }
}

/** Toda petición pasa por aquí: usa la URL de la conexión activa (local o nube). */
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