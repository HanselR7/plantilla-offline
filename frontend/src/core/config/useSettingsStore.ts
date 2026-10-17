import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type AppRole = 'server' | 'client' | 'cloud'

interface SettingsValues {
    role: AppRole | null
    /** URL de la API del servidor local (null en modo solo nube). */
    localUrl: string | null
    /** URL de la API en la nube (opcional salvo en modo solo nube). */
    cloudUrl: string | null
}

interface SettingsStore extends SettingsValues {
    configure: (values: Omit<SettingsValues, 'role'> & { role: AppRole }) => void
    reset: () => void
}

const empty: SettingsValues = { role: null, localUrl: null, cloudUrl: null }

export const useSettingsStore = create<SettingsStore>()(
    persist(
        (set) => ({
            ...empty,
            configure: (values) => set(values),
            reset: () => set(empty),
        }),
        { name: 'plantilla.settings' },
    ),
)