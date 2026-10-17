import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { ConfigProvider } from 'antd'
import App from './app/App'
import { theme } from './app/theme'
import './styles/main.scss'

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <ConfigProvider theme={theme}>
            <App />
        </ConfigProvider>
    </StrictMode>,
)