import { appConfig } from '../config/appConfig'
import { checkHealth } from './healthCheck'
import type { ConnectionState } from './types'

function getLocalUrl(): string {
    return localStorage.getItem(appConfig.localServerStorageKey) ?? appConfig.defaultLocalUrl
}

/** Orden de prioridad: servidor local → nube → offline. */
export async function resolveConnection(): Promise<ConnectionState> {
    const localUrl = getLocalUrl()
    const local = await checkHealth(localUrl)
    if (local) return { mode: 'local', apiUrl: localUrl, server: local.server }

    const cloud = await checkHealth(appConfig.cloudApiUrl)
    if (cloud) return { mode: 'cloud', apiUrl: appConfig.cloudApiUrl, server: cloud.server }

    return { mode: 'offline', apiUrl: null }
}