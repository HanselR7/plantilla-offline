import { create } from 'zustand'
import { appConfig } from '../config/appConfig'
import { resolveConnection } from './connectionManager'
import type { ConnectionState } from './types'

interface ConnectionStore extends ConnectionState {
    refresh: () => Promise<void>
    /** Arranca el chequeo periódico. Devuelve la función para detenerlo. */
    start: () => () => void
}

export const useConnectionStore = create<ConnectionStore>((set, get) => ({
    mode: 'checking',
    apiUrl: null,

    refresh: async () => {
        set(await resolveConnection())
    },

    start: () => {
        const { refresh } = get()
        const first = setTimeout(() => void refresh(), 0)
        const timer = setInterval(() => void refresh(), appConfig.checkIntervalMs)
        const onNetworkChange = () => void refresh()
        window.addEventListener('online', onNetworkChange)
        window.addEventListener('offline', onNetworkChange)
        return () => {
            clearTimeout(first)
            clearInterval(timer)
            window.removeEventListener('online', onNetworkChange)
            window.removeEventListener('offline', onNetworkChange)
        }
    },
}))