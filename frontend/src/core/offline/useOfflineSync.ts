import { useEffect } from 'react'
import { useConnectionStore } from '../connectivity/useConnectionStore'
import { processQueue } from './queueProcessor'
import { useQueueStore } from './useQueueStore'

/** Al recuperar conexión (local o nube), vacía la cola. */
export function useOfflineSync() {
    const apiUrl = useConnectionStore((s) => s.apiUrl)
    const refreshCount = useQueueStore((s) => s.refreshCount)

    useEffect(() => {
        void refreshCount()
    }, [refreshCount])

    useEffect(() => {
        if (apiUrl) void processQueue()
    }, [apiUrl])
}