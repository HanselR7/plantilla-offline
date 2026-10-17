import { create } from 'zustand'
import { queueDb } from './queueDb'

interface QueueStore {
    pending: number
    syncing: boolean
    setSyncing: (v: boolean) => void
    refreshCount: () => Promise<void>
}

export const useQueueStore = create<QueueStore>((set) => ({
    pending: 0,
    syncing: false,
    setSyncing: (syncing) => set({ syncing }),
    refreshCount: async () => set({ pending: await queueDb.count() }),
}))