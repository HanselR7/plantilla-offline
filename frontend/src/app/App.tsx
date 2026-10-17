import { useEffect } from 'react'
import { Layout, Typography } from 'antd'
import { ConnectionBadge } from '../shared/components/ConnectionBadge'
import { PendingBadge } from '../shared/components/PendingBadge'
import { useConnectionStore } from '../core/connectivity/useConnectionStore'
import { useOfflineSync } from '../core/offline/useOfflineSync'
import { NotasPage } from '../features/notas/NotasPage'

const { Header, Content } = Layout

export default function App() {
    const start = useConnectionStore((s) => s.start)
    useEffect(() => start(), [start])
    useOfflineSync()

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Typography.Text style={{ color: '#fff', fontSize: 18 }}>Plantilla Offline</Typography.Text>
                <div>
                    <PendingBadge />
                    <ConnectionBadge />
                </div>
            </Header>
            <Content style={{ padding: 24 }}>
                <NotasPage />
            </Content>
        </Layout>
    )
}