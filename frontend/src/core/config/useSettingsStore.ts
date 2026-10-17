import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type AppRole = 'server' | 'client' | 'cloud'

interface SettingsValues {
    role: AppRole | null
    localUrl: string | null
    cloudUrl: string | null
    deviceId: string | null
    deviceToken: string | null
}

interface SettingsStore extends SettingsValues {
    configure: (
        values: Pick<SettingsValues, 'localUrl' | 'cloudUrl'> &
            Partial<Pick<SettingsValues, 'deviceId' | 'deviceToken'>> & { role: AppRole },
    ) => void
    reset: () => void
}

const empty: SettingsValues = {
    role: null,
    localUrl: null,
    cloudUrl: null,
    deviceId: null,
    deviceToken: null,
}

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