import { useQueueStore } from '../../core/offline/useQueueStore'

export function PendingBadge() {
    const pending = useQueueStore((s) => s.pending)
    const syncing = useQueueStore((s) => s.syncing)
    if (pending === 0) return null
    return (
        <span style={{ color: '#fff', marginRight: 12, fontSize: 13 }}>
            {syncing ? 'Sincronizando…' : `${pending} pendiente${pending > 1 ? 's' : ''}`}
        </span>
    )
}