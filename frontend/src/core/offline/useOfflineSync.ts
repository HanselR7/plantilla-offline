import { useEffect } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { useCurrentSession } from '../auth/useCurrentSession'
import { useConnectionStore } from '../connectivity/useConnectionStore'
import { processQueue } from './queueProcessor'
import { useQueueStore } from './useQueueStore'

/** Al recuperar conexión o iniciar sesión, vacía la cola y refresca los datos. */
export function useOfflineSync() {
    const apiUrl = useConnectionStore((s) => s.apiUrl)
    const loggedIn = useCurrentSession() !== null
    const refreshCount = useQueueStore((s) => s.refreshCount)
    const queryClient = useQueryClient()

    useEffect(() => {
        void refreshCount()
    }, [refreshCount])

    useEffect(() => {
        if (!apiUrl || !loggedIn) return
        void processQueue().then(() => queryClient.invalidateQueries())
    }, [apiUrl, loggedIn, queryClient])
}