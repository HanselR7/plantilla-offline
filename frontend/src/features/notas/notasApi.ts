import { v7 as uuidv7 } from 'uuid'
import { useConnectionStore } from '../../core/connectivity/useConnectionStore'
import { http, write } from '../../core/http/httpClient'
import { applyPending } from '../../core/offline/pendingOverlay'
import { snapshotDb } from '../../core/offline/snapshotDb'
import type { Nota, NotaInput } from './types'

const SNAPSHOT_KEY = 'notas'

async function listar(): Promise<Nota[]> {
    const { apiUrl } = useConnectionStore.getState()

    if (apiUrl) {
        try {
            const fresh = await http<Nota[]>('/notas')
            await snapshotDb.put(SNAPSHOT_KEY, fresh)
            return applyPending(fresh, '/notas')
        } catch (e) {
            // Error de negocio del servidor: se muestra. Error de red: se usa la copia local.
            if (!(e instanceof TypeError)) throw e
        }
    }

    const cached = (await snapshotDb.get<Nota[]>(SNAPSHOT_KEY)) ?? []
    return applyPending(cached, '/notas')
}

export const notasApi = {
    listar,
    crear: (data: NotaInput) => write<Nota>('POST', '/notas', { id: uuidv7(), ...data }),
    actualizar: (id: string, data: NotaInput) => write<Nota>('PUT', `/notas/${id}`, data),
    eliminar: (id: string) => write<void>('DELETE', `/notas/${id}`),
}