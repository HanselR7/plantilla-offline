const API_PATH = '/plantilla-api/api'

/** Acepta "192.168.1.45", "192.168.1.45:8080" o una URL completa. */
export function normalizeLocalUrl(input: string): string {
    const value = input.trim().replace(/\/+$/, '')
    if (/^https?:\/\//i.test(value)) return value
    const host = value.includes(':') ? value : `${value}:8080`
    return `http://${host}${API_PATH}`
}

export function normalizeCloudUrl(input: string): string {
    return input.trim().replace(/\/+$/, '')
}