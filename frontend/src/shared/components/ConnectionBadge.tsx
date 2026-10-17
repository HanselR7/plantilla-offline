import { useConnectionStore } from '../../core/connectivity/useConnectionStore'
import './ConnectionBadge.scss'

const LABELS = {
    checking: 'Comprobando…',
    local: 'Servidor local',
    cloud: 'Nube',
    offline: 'Sin conexión',
} as const

export function ConnectionBadge() {
    const mode = useConnectionStore((s) => s.mode)
    const server = useConnectionStore((s) => s.server)
    return (
        <span className={`connection-badge connection-badge--${mode}`}>
            <span className="connection-badge__label">{LABELS[mode]}</span>
            {server && <span className="connection-badge__detail">{server.name}</span>}
        </span>
    )
}