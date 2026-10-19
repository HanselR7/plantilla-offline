import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface AuthUser {
    id: string
    username: string
    role: 'ADMIN' | 'USER'
}

export interface Session {
    accessToken: string
    refreshToken: string
    user: AuthUser
}

interface AuthStore {
    /** Una sesión por servidor: el local y la nube tienen cuentas y tokens independientes. */
    sessions: Record<string, Session>
    setSession: (apiUrl: string, session: Session) => void
    clearSession: (apiUrl: string) => void
}

export const useAuthStore = create<AuthStore>()(
    persist(
        (set) => ({
            sessions: {},
            setSession: (apiUrl, session) =>
                set((s) => ({ sessions: { ...s.sessions, [apiUrl]: session } })),
            clearSession: (apiUrl) =>
                set((s) => {
                    const sessions = { ...s.sessions }
                    delete sessions[apiUrl]
                    return { sessions }
                }),
        }),
        { name: 'plantilla.auth' },
    ),
)