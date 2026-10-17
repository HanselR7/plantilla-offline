import { http } from '../../core/http/httpClient'
import type { Nota, NotaInput } from './types'

export const notasApi = {
    listar: () => http<Nota[]>('/notas'),
    crear: (data: NotaInput) =>
        http<Nota>('/notas', { method: 'POST', body: JSON.stringify(data) }),
    actualizar: (id: string, data: NotaInput) =>
        http<Nota>(`/notas/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    eliminar: (id: string) => http<void>(`/notas/${id}`, { method: 'DELETE' }),
}