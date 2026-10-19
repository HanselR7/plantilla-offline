import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useConnectionStore } from '../../core/connectivity/useConnectionStore'
import { notasApi } from './notasApi'
import type { NotaInput } from './types'

const KEY = ['notas']

export function useNotas() {
    const apiUrl = useConnectionStore((s) => s.apiUrl)
    return useQuery({
        queryKey: [...KEY, apiUrl],
        queryFn: notasApi.listar,
    })
}

export function useGuardarNota() {
    const qc = useQueryClient()
    return useMutation({
        mutationFn: ({ id, data }: { id?: string; data: NotaInput }) =>
            id ? notasApi.actualizar(id, data) : notasApi.crear(data),
        onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
    })
}

export function useEliminarNota() {
    const qc = useQueryClient()
    return useMutation({
        mutationFn: (id: string) => notasApi.eliminar(id),
        onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
    })
}