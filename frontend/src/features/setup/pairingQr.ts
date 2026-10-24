export interface PairingQr {
    api: string
    code: string
}

/** Valida el contenido del QR. Devuelve null si no es de este sistema. */
export function parsePairingQr(text: string): PairingQr | null {
    try {
        const data = JSON.parse(text) as { v?: number; api?: unknown; code?: unknown }
        if (data.v !== 1 || typeof data.api !== 'string' || typeof data.code !== 'string') return null
        if (!/^https?:\/\/\S+$/i.test(data.api)) return null
        if (!/^[A-Za-z0-9]{8}$/.test(data.code)) return null
        return { api: data.api.replace(/\/+$/, ''), code: data.code.toUpperCase() }
    } catch {
        return null
    }
}