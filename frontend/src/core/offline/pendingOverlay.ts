import { queueDb } from './queueDb'

/**
 * Aplica sobre una lista las operaciones pendientes de la cola que afectan a `basePath`
 * (por ejemplo "/notas"), para que lo hecho sin conexión se vea de inmediato.
 */
export async function applyPending<T extends { id: string }>(list: T[], basePath: string): Promise<T[]> {
    const result = new Map(list.map((item) => [item.id, item]))

    for (const op of await queueDb.all()) {
        if (op.path !== basePath && !op.path.startsWith(`${basePath}/`)) continue
        const body = op.body ? (JSON.parse(op.body) as Partial<T>) : null

        if (op.method === 'POST' && body?.id) {
            result.set(body.id, { ...(result.get(body.id) ?? {}), ...body } as T)
        } else if (op.method === 'PUT') {
            const id = op.path.slice(basePath.length + 1)
            const current = result.get(id)
            if (current && body) result.set(id, { ...current, ...body, id })
        } else if (op.method === 'DELETE') {
            result.delete(op.path.slice(basePath.length + 1))
        }
    }
    return [...result.values()]
}