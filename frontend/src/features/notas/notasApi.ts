import { v7 as uuidv7 } from 'uuid'
import { http, write } from '../../core/http/httpClient'
import type { Nota, NotaInput } from './types'

export const notasApi = {
    listar: () => http<Nota[]>('/notas'),
    crear: (data: NotaInput) => write<Nota>('POST', '/notas', { id: uuidv7(), ...data }),
    actualizar: (id: string, data: NotaInput) => write<Nota>('PUT', `/notas/${id}`, data),
    eliminar: (id: string) => write<void>('DELETE', `/notas/${id}`),
}