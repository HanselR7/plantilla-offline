import { useState } from 'react'
import { Alert, Button, Card, Form, Input, Typography } from 'antd'
import { useSettingsStore } from '../../core/config/useSettingsStore'
import type { AppRole } from '../../core/config/useSettingsStore'
import { appConfig } from '../../core/config/appConfig'
import { detectEmbeddedApiUrl, normalizeCloudUrl, normalizeLocalUrl } from '../../core/config/urlUtils'
import { useConnectionStore } from '../../core/connectivity/useConnectionStore'
import { redeemPairingCode } from './pairingApi'
import './SetupWizard.scss'
import useApp from 'antd/es/app/useApp'
import { discoverServer, rememberServer } from '../../core/connectivity/discovery'
import { Progress } from 'antd'

interface FormValues {
    localUrl?: string
    cloudUrl?: string
    pairCode?: string
}

const OPTIONS: { role: AppRole; title: string; text: string }[] = [
    {
        role: 'server',
        title: 'Servidor local',
        text: 'Este equipo tiene PostgreSQL y el backend, y atenderá a los demás dispositivos de la red.',
    },
    {
        role: 'client',
        title: 'Cliente',
        text: 'Este dispositivo se conecta a un servidor local que ya existe en la red.',
    },
    {
        role: 'cloud',
        title: 'Solo nube',
        text: 'Sin servidor local: este dispositivo trabaja directamente con la nube.',
    },
]

const isHttpUrl = (value: string) => /^https?:\/\/\S+$/i.test(value.trim())

export function SetupWizard() {
    const { message } = useApp();
    const configure = useSettingsStore((s) => s.configure)
    const [role, setRole] = useState<AppRole | null>(null)
    const [form] = Form.useForm<FormValues>()
    const embeddedUrl = detectEmbeddedApiUrl()
    const [searching, setSearching] = useState(false)
    const [step, setStep] = useState('')
    const [progress, setProgress] = useState(0)
    const [subnet, setSubnet] = useState('')

    const search = async () => {
        setSearching(true)
        setProgress(0)
        const found = await discoverServer({
            subnetPrefix: subnet.trim() || undefined,
            onStep: setStep,
            onProgress: (n) => setProgress(Math.round((n / 254) * 100)),
        })
        setSearching(false)
        if (found) {
            form.setFieldValue('localUrl', found.apiUrl)
            void message.success(`Servidor encontrado: ${found.health.server?.name ?? found.apiUrl}`)
        } else {
            void message.warning('No se encontró el servidor. Escribe su dirección o indica tu red.')
        }
    }

    const onFinish = async (values: FormValues) => {
        if (!role) return
        let cloudUrl = values.cloudUrl?.trim() ? normalizeCloudUrl(values.cloudUrl) : null
        let localUrl: string | null = null
        let deviceId: string | null = null
        let deviceToken: string | null = null

        if (role === 'server') localUrl = embeddedUrl ?? appConfig.defaultLocalUrl

        if (role === 'client') {
            localUrl = normalizeLocalUrl(values.localUrl ?? '')
            if (values.pairCode?.trim()) {
                try {
                    const paired = await redeemPairingCode(localUrl, values.pairCode, navigator.userAgent.slice(0, 60))
                    deviceId = paired.deviceId
                    deviceToken = paired.deviceToken
                    cloudUrl = cloudUrl ?? paired.cloudUrl
                } catch (e) {
                    void message.error((e as Error).message)
                    return
                }
            }
        }

        configure({ role, localUrl, cloudUrl, deviceId, deviceToken })
        if (localUrl) rememberServer(localUrl)
        void useConnectionStore.getState().refresh()
    }

    if (!role) {
        return (
            <div className="setup-wizard">
                <Typography.Title level={2}>Configuración inicial</Typography.Title>
                <Typography.Paragraph>¿Cómo se usará este dispositivo?</Typography.Paragraph>
                <div className="setup-wizard__options">
                    {OPTIONS.map((o) => (
                        <Card key={o.role} className="setup-wizard__option" onClick={() => setRole(o.role)}>
                            <Typography.Title level={4} style={{ marginTop: 0 }}>{o.title}</Typography.Title>
                            <Typography.Text>{o.text}</Typography.Text>
                        </Card>
                    ))}
                </div>
            </div>
        )
    }

    return (
        <div className="setup-wizard">
            <Typography.Title level={2}>{OPTIONS.find((o) => o.role === role)?.title}</Typography.Title>

            {role === 'cloud' && (
                <Alert
                    type="warning"
                    showIcon
                    style={{ marginBottom: 16 }}
                    title="Sin servidor local, la app necesita internet para compartir datos entre dispositivos. Sin internet, los cambios se guardan en este dispositivo y se envían al reconectar."
                />
            )}
            {role === 'server' && (
                <Alert
                    type="info"
                    showIcon
                    style={{ marginBottom: 16 }}
                    title="El instalador ya configuró la base de datos de este equipo. Solo falta, si quieres sincronizar, la dirección de la nube."
                />
            )}
            {role === 'client' && (
                <Alert
                    type="info"
                    showIcon
                    style={{ marginBottom: 16 }}
                    title={
                        embeddedUrl
                            ? 'Detectamos el servidor desde el que abriste esta app. Confirma la dirección o cámbiala.'
                            : 'Más adelante el servidor se descubrirá solo o con un QR. Por ahora, escribe su dirección.'
                    }
                />
            )}

            <Form form={form} layout="vertical" onFinish={onFinish} initialValues={{ localUrl: embeddedUrl ?? undefined }}>
                {role === 'client' && (
                    <div style={{ marginBottom: 16 }}>
                        <Button onClick={() => void search()} loading={searching}>
                            Buscar servidor automáticamente
                        </Button>
                        {searching && (
                            <div style={{ marginTop: 8 }}>
                                <Typography.Text type="secondary">{step}</Typography.Text>
                                {progress > 0 && <Progress percent={progress} size="small" />}
                            </div>
                        )}
                        <Input
                            style={{ marginTop: 8 }}
                            placeholder="Opcional: tu red, ej. 192.168.1 (para escanear)"
                            value={subnet}
                            onChange={(e) => setSubnet(e.target.value)}
                        />
                    </div>
                )}

                {role === 'client' && (
                    <Form.Item
                        name="localUrl"
                        label="Dirección del servidor local"
                        extra="Ejemplo: 192.168.1.45 (el puerto 8080 se agrega solo)"
                        rules={[{ required: true, message: 'Escribe la dirección del servidor' }]}
                    >
                        <Input placeholder="192.168.1.45" />
                    </Form.Item>
                )}

                {role === 'client' && (
                    <Form.Item
                        name="pairCode"
                        label="Código de emparejamiento"
                        extra="Se muestra en el servidor, en «Emparejar dispositivo». Es opcional por ahora."
                    >
                        <Input placeholder="ABCD2345" maxLength={8} style={{ textTransform: 'uppercase' }} />
                    </Form.Item>
                )}

                <Form.Item
                    name="cloudUrl"
                    label={role === 'cloud' ? 'URL de la API en la nube' : 'URL de la API en la nube (opcional)'}
                    rules={[
                        { required: role === 'cloud', message: 'La URL de la nube es obligatoria' },
                        {
                            validator: (_, value: string | undefined) =>
                                !value || isHttpUrl(value)
                                    ? Promise.resolve()
                                    : Promise.reject(new Error('Debe empezar con http:// o https://')),
                        },
                    ]}
                >
                    <Input placeholder="https://mi-api.run.app/plantilla-api/api" />
                </Form.Item>

                <div className="setup-wizard__actions">
                    <Button onClick={() => setRole(null)}>Atrás</Button>
                    <Button type="primary" htmlType="submit">Guardar y continuar</Button>
                </div>
            </Form>
        </div>
    )
}