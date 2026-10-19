import { snapshotDb } from '../offline/snapshotDb'
import { authFetch } from './authFetch'
import { useAuthStore } from './authStore'
import type { Session } from './authStore'

export async function login(apiUrl: string, username: string, password: string): Promise<Session> {
    const res = await fetch(`${apiUrl}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
    })
    if (res.status === 401) throw new Error('Usuario o contraseña incorrectos')
    if (!res.ok) throw new Error('No se pudo iniciar sesión')
    const body = (await res.json()) as Session
    const session: Session = {
        accessToken: body.accessToken,
        refreshToken: body.refreshToken,
        user: body.user,
    }
    useAuthStore.getState().setSession(apiUrl, session)
    return session
}

export async function logout(apiUrl: string): Promise<void> {
    try {
        await authFetch(`${apiUrl}/auth/logout`, { method: 'POST' })
    } catch {
        // Sin red: la sesión se cierra igual en este dispositivo.
    }
    useAuthStore.getState().clearSession(apiUrl)
    // Los datos copiados para uso offline no deben quedar al alcance de la siguiente persona.
    await snapshotDb.clear()
}