import { useEffect } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { useConnectionStore } from '../connectivity/useConnectionStore'
import { processQueue } from './queueProcessor'
import { useQueueStore } from './useQueueStore'

/** Al recuperar conexión (local o nube), vacía la cola y refresca los datos. */
export function useOfflineSync() {
    const apiUrl = useConnectionStore((s) => s.apiUrl)
    const refreshCount = useQueueStore((s) => s.refreshCount)
    const queryClient = useQueryClient()

    useEffect(() => {
        void refreshCount()
    }, [refreshCount])

    useEffect(() => {
        if (!apiUrl) return
        void processQueue().then(() => queryClient.invalidateQueries())
    }, [apiUrl, queryClient])
}