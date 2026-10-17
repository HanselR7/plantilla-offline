import { useEffect } from 'react'
import { Button, Layout, Popconfirm, Typography } from 'antd'
import { SettingOutlined } from '@ant-design/icons'
import { ConnectionBadge } from '../shared/components/ConnectionBadge'
import { PendingBadge } from '../shared/components/PendingBadge'
import { useConnectionStore } from '../core/connectivity/useConnectionStore'
import { useSettingsStore } from '../core/config/useSettingsStore'
import { useOfflineSync } from '../core/offline/useOfflineSync'
import { NotasPage } from '../features/notas/NotasPage'
import { SetupWizard } from '../features/setup/SetupWizard'
import { PairDevicePanel } from '../features/setup/PairDevicePanel'

const { Header, Content } = Layout

export default function App() {
    const start = useConnectionStore((s) => s.start)
    const role = useSettingsStore((s) => s.role)
    const reset = useSettingsStore((s) => s.reset)

    useEffect(() => start(), [start])
    useOfflineSync()

    if (!role) {
        return (
            <Layout style={{ minHeight: '100vh' }}>
                <Content style={{ padding: 24 }}>
                    <SetupWizard />
                </Content>
            </Layout>
        )
    }

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Typography.Text style={{ color: '#fff', fontSize: 18 }}>Plantilla Offline</Typography.Text>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <PendingBadge />
                    <ConnectionBadge />
                    {role === 'server' && <PairDevicePanel />}
                    <Popconfirm
                        title="¿Volver a configurar este dispositivo?"
                        description="Los cambios pendientes de envío se conservan."
                        onConfirm={reset}
                    >
                        <Button type="text" icon={<SettingOutlined style={{ color: '#fff' }} />} />
                    </Popconfirm>
                </div>
            </Header>
            <Content style={{ padding: 24 }}>
                <NotasPage />
            </Content>
        </Layout>
    )
}