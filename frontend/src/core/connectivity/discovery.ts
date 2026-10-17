import { checkHealth } from './healthCheck'
import { detectEmbeddedApiUrl } from '../config/urlUtils'
import type { ServerHealth } from './types'

const LAST_KEY = 'plantilla.lastServerUrl'
const API_PATH = '/plantilla-api/api'

export interface Discovered {
    apiUrl: string
    health: ServerHealth
}

const urlFor = (host: string) => `http://${host}:8080${API_PATH}`

async function tryUrl(apiUrl: string): Promise<Discovered | null> {
    const health = await checkHealth(apiUrl, 1200)
    return health ? { apiUrl, health } : null
}

export function rememberServer(apiUrl: string) {
    localStorage.setItem(LAST_KEY, apiUrl)
}

/** Prueba varias URLs en paralelo y devuelve la primera que responde. */
async function firstThatResponds(urls: string[]): Promise<Discovered | null> {
    if (urls.length === 0) return null
    return new Promise((resolve) => {
        let pending = urls.length
        for (const url of urls) {
            void tryUrl(url).then((found) => {
                if (found) resolve(found)
                else if (--pending === 0) resolve(null)
            })
        }
    })
}

/** Limita la concurrencia para no saturar el navegador al escanear. */
async function scanSubnet(prefix: string, onProgress?: (done: number) => void): Promise<Discovered | null> {
    const hosts = Array.from({ length: 254 }, (_, i) => `${prefix}.${i + 1}`)
    let index = 0
    let done = 0
    let found: Discovered | null = null

    const worker = async () => {
        while (!found && index < hosts.length) {
            const host = hosts[index++]
            const result = await tryUrl(urlFor(host))
            done++
            onProgress?.(done)
            if (result) found = result
        }
    }
    await Promise.all(Array.from({ length: 24 }, worker))
    return found
}

export interface DiscoveryOptions {
    /** Pista de red, por ejemplo "192.168.1". Solo si se quiere escanear. */
    subnetPrefix?: string
    onStep?: (label: string) => void
    onProgress?: (done: number) => void
}

export async function discoverServer(opts: DiscoveryOptions = {}): Promise<Discovered | null> {
    opts.onStep?.('Buscando en el origen de la página…')
    const embedded = detectEmbeddedApiUrl()
    if (embedded) {
        const found = await tryUrl(embedded)
        if (found) return found
    }

    opts.onStep?.('Probando el último servidor usado…')
    const last = localStorage.getItem(LAST_KEY)
    if (last) {
        const found = await tryUrl(last)
        if (found) return found
    }

    opts.onStep?.('Probando nombres comunes…')
    const named = await firstThatResponds(['servidor.local', 'plantilla.local'].map(urlFor))
    if (named) return named

    if (opts.subnetPrefix && /^\d{1,3}\.\d{1,3}\.\d{1,3}$/.test(opts.subnetPrefix)) {
        opts.onStep?.(`Escaneando ${opts.subnetPrefix}.1–254…`)
        return scanSubnet(opts.subnetPrefix, opts.onProgress)
    }

    return null
}
