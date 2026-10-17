import { useState } from 'react'
import { Alert, Button, Card, Form, Input, Typography } from 'antd'
import { useSettingsStore } from '../../core/config/useSettingsStore'
import type { AppRole } from '../../core/config/useSettingsStore'
import { appConfig } from '../../core/config/appConfig'
import { detectEmbeddedApiUrl, normalizeCloudUrl, normalizeLocalUrl } from '../../core/config/urlUtils'
import { useConnectionStore } from '../../core/connectivity/useConnectionStore'
import './SetupWizard.scss'

interface FormValues {
    localUrl?: string
    cloudUrl?: string
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
    const configure = useSettingsStore((s) => s.configure)
    const [role, setRole] = useState<AppRole | null>(null)
    const [form] = Form.useForm<FormValues>()
    const embeddedUrl = detectEmbeddedApiUrl()

    const onFinish = (values: FormValues) => {
        if (!role) return
        const cloudUrl = values.cloudUrl?.trim() ? normalizeCloudUrl(values.cloudUrl) : null
        let localUrl: string | null = null
        if (role === 'server') localUrl = embeddedUrl ?? appConfig.defaultLocalUrl
        if (role === 'client' && values.localUrl?.trim()) localUrl = normalizeLocalUrl(values.localUrl)

        configure({ role, localUrl, cloudUrl })
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
                    <Form.Item
                        name="localUrl"
                        label="Dirección del servidor local"
                        extra="Ejemplo: 192.168.1.45 (el puerto 8080 se agrega solo)"
                        rules={[{ required: true, message: 'Escribe la dirección del servidor' }]}
                    >
                        <Input placeholder="192.168.1.45" />
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