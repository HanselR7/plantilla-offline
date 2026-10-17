import { useSettingsStore } from '../config/useSettingsStore'
import { checkHealth } from './healthCheck'
import type { ConnectionState } from './types'

/** Orden de prioridad: servidor local → nube → offline. Respeta el rol configurado. */
export async function resolveConnection(): Promise<ConnectionState> {
    const { role, localUrl, cloudUrl } = useSettingsStore.getState()
    if (role === null) return { mode: 'offline', apiUrl: null }

    if (role !== 'cloud' && localUrl) {
        const local = await checkHealth(localUrl)
        if (local) return { mode: 'local', apiUrl: localUrl, server: local.server }
    }

    if (cloudUrl) {
        const cloud = await checkHealth(cloudUrl)
        if (cloud) return { mode: 'cloud', apiUrl: cloudUrl, server: cloud.server }
    }

    return { mode: 'offline', apiUrl: null }
}