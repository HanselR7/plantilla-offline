import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { ConfigProvider } from 'antd'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { App as AntApp } from 'antd'
import { theme } from './app/theme'
import './styles/main.scss'
import App from './app/App'

const queryClient = new QueryClient({
    defaultOptions: {
        queries: { retry: false, refetchOnWindowFocus: false, networkMode: 'always' },
        mutations: { networkMode: 'always' },
    },
})

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <ConfigProvider theme={theme}>
            <QueryClientProvider client={queryClient}>
                <AntApp >
                    <App />
                </AntApp>
            </QueryClientProvider>
        </ConfigProvider>
    </StrictMode>,
)