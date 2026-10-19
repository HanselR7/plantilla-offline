import { useState } from 'react'
import { Alert, Button, Card, Form, Input, Typography } from 'antd'
import { login } from '../../core/auth/authApi'
import { useConnectionStore } from '../../core/connectivity/useConnectionStore'
import './LoginPage.scss'

interface Values {
    username: string
    password: string
}

const TARGET = {
    checking: 'Comprobando conexión…',
    local: 'Iniciando sesión en el servidor local',
    cloud: 'Iniciando sesión en la nube',
    offline: '',
} as const

export function LoginPage() {
    const apiUrl = useConnectionStore((s) => s.apiUrl)
    const mode = useConnectionStore((s) => s.mode)
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const onFinish = async (values: Values) => {
        if (!apiUrl) return
        setLoading(true)
        setError(null)
        try {
            await login(apiUrl, values.username, values.password)
        } catch (e) {
            setError(e instanceof TypeError ? 'No hay conexión con el servidor' : (e as Error).message)
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="login-page">
            <Card>
                <Typography.Title level={3} style={{ marginTop: 0 }}>Iniciar sesión</Typography.Title>
                {mode === 'offline' ? (
                    <Alert
                        className="login-page__hint"
                        type="warning"
                        showIcon
                        title="Sin conexión: el primer inicio de sesión en este dispositivo necesita llegar al servidor."
                    />
                ) : (
                    <Typography.Paragraph type="secondary">{TARGET[mode]}</Typography.Paragraph>
                )}
                {error && <Alert className="login-page__hint" type="error" showIcon title={error} />}
                <Form layout="vertical" onFinish={(v) => void onFinish(v)}>
                    <Form.Item name="username" label="Usuario" rules={[{ required: true, message: 'Escribe tu usuario' }]}>
                        <Input autoComplete="username" />
                    </Form.Item>
                    <Form.Item name="password" label="Contraseña" rules={[{ required: true, message: 'Escribe tu contraseña' }]}>
                        <Input.Password autoComplete="current-password" />
                    </Form.Item>
                    <Button type="primary" htmlType="submit" block loading={loading} disabled={!apiUrl}>
                        Entrar
                    </Button>
                </Form>
            </Card>
        </div>
    )
}