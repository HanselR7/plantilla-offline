import { useState } from 'react'
import { Alert, Button, Modal, Typography } from 'antd'
import { QRCodeSVG } from 'qrcode.react'
import { useSettingsStore } from '../../core/config/useSettingsStore'
import { createPairingCode } from './pairingApi'
import type { PairingCode } from './pairingApi'

export function PairDevicePanel() {
    const localUrl = useSettingsStore((s) => s.localUrl)
    const [open, setOpen] = useState(false)
    const [code, setCode] = useState<PairingCode | null>(null)
    const [error, setError] = useState<string | null>(null)

    const generate = async () => {
        setError(null)
        setCode(null)
        setOpen(true)
        try {
            setCode(await createPairingCode(localUrl!))
        } catch (e) {
            setError((e as Error).message)
        }
    }

    // El QR lleva el código y la dirección con la que este servidor es alcanzable.
    const qrValue = code
        ? JSON.stringify({ v: 1, api: localUrl, code: code.code })
        : ''

    return (
        <>
            <Button type="text" style={{ color: '#fff' }} onClick={() => void generate()}>
                Emparejar dispositivo
            </Button>
            <Modal title="Emparejar dispositivo" open={open} footer={null} onCancel={() => setOpen(false)}>
                {error && <Alert type="error" showIcon title={error} />}
                {code && (
                    <div style={{ textAlign: 'center' }}>
                        <QRCodeSVG value={qrValue} size={220} />
                        <Typography.Title level={2} copyable style={{ letterSpacing: 4 }}>
                            {code.code}
                        </Typography.Title>
                        <Typography.Text type="secondary">
                            Vence a las {new Date(code.expiresAt).toLocaleTimeString()} y solo se puede usar una vez.
                        </Typography.Text>
                        <Alert
                            style={{ marginTop: 16, textAlign: 'left' }}
                            type="warning"
                            showIcon
                            title="Si abres esta pantalla en localhost, el QR llevará 'localhost', que no sirve en otro dispositivo. Abre la app desde la IP del servidor (http://IP:8080/plantilla-api/) para generar un QR utilizable."
                        />
                    </div>
                )}
            </Modal>
        </>
    )
}