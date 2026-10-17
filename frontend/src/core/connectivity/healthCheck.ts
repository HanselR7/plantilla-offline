import { appConfig } from '../config/appConfig'
import type { ServerHealth } from './types'

/** Consulta /health con tiempo límite. Devuelve null si no responde o está degradado. */
export async function checkHealth(apiUrl: string): Promise<ServerHealth | null> {
    const controller = new AbortController()
    const timer = setTimeout(() => controller.abort(), appConfig.healthTimeoutMs)
    try {
        const res = await fetch(`${apiUrl}/health`, { signal: controller.signal })
        if (!res.ok) return null
        const body = (await res.json()) as ServerHealth
        return body.status === 'UP' && body.database ? body : null
    } catch {
        return null
    } finally {
        clearTimeout(timer)
    }
}