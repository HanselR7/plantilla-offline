import { useAuthStore } from './authStore'
import type { Session } from './authStore'

const inflight = new Map<string, Promise<boolean>>()

/** La sesión se elige por la URL: pertenece al servidor al que se llama. */
function sessionKeyFor(url: string): string | null {
    return Object.keys(useAuthStore.getState().sessions).find((k) => url.startsWith(k)) ?? null
}

/** Una sola renovación a la vez por servidor, aunque fallen varias peticiones juntas. */
function refreshSession(key: string): Promise<boolean> {
    const existing = inflight.get(key)
    if (existing) return existing

    const promise = (async () => {
        const current = useAuthStore.getState().sessions[key]
        if (!current) return false
        try {
            const res = await fetch(`${key}/auth/refresh`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken: current.refreshToken }),
            })
            if (res.ok) {
                const body = (await res.json()) as Session
                useAuthStore.getState().setSession(key, {
                    accessToken: body.accessToken,
                    refreshToken: body.refreshToken,
                    user: body.user,
                })
                return true
            }
            // El servidor rechazó la renovación: la sesión terminó de verdad.
            if (res.status === 401) useAuthStore.getState().clearSession(key)
            return false
        } catch {
            // Sin red: no se cierra la sesión, se reintentará después.
            return false
        }
    })().finally(() => inflight.delete(key))

    inflight.set(key, promise)
    return promise
}

export async function authFetch(url: string, init: RequestInit = {}): Promise<Response> {
    const key = sessionKeyFor(url)
    const send = () => {
        const headers = new Headers(init.headers)
        const token = key ? useAuthStore.getState().sessions[key]?.accessToken : null
        if (token) headers.set('Authorization', `Bearer ${token}`)
        return fetch(url, { ...init, headers })
    }

    const res = await send()
    if (res.status !== 401 || !key) return res
    return (await refreshSession(key)) ? send() : res
}