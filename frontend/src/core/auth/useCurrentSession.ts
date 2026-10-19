import { useConnectionStore } from '../connectivity/useConnectionStore'
import { useAuthStore } from './authStore'
import type { Session } from './authStore'

/**
 * Sesión del servidor activo. Sin servidor (offline) se conserva la sesión existente,
 * para poder seguir trabajando sin volver a escribir la clave.
 */
export function useCurrentSession(): { apiUrl: string; session: Session } | null {
    const active = useConnectionStore((s) => s.apiUrl)
    const sessions = useAuthStore((s) => s.sessions)
    if (active) return sessions[active] ? { apiUrl: active, session: sessions[active] } : null
    const first = Object.entries(sessions)[0]
    return first ? { apiUrl: first[0], session: first[1] } : null
}