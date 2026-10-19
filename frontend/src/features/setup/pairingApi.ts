import { authFetch } from "../../core/auth/authFetch"

export interface PairingResult {
    deviceId: string
    deviceToken: string
    cloudUrl: string | null
}

export interface PairingCode {
    code: string
    expiresAt: string
}

export async function createPairingCode(apiUrl: string): Promise<PairingCode> {
    const res = await authFetch(`${apiUrl}/pairing/codes`, { method: 'POST' })
    if (res.status === 403) throw new Error('Solo un administrador puede emparejar dispositivos')
    if (!res.ok) throw new Error('No se pudo generar el código')
    return (await res.json()) as PairingCode
}

export async function redeemPairingCode(
    apiUrl: string,
    code: string,
    deviceName: string,
): Promise<PairingResult> {
    const res = await fetch(`${apiUrl}/pairing/redeem`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ code, deviceName }),
    })
    if (res.status === 401) throw new Error('Código inválido o vencido')
    if (!res.ok) throw new Error('No se pudo emparejar con el servidor')
    return (await res.json()) as PairingResult
}