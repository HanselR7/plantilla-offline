import { useEffect, useRef, useState } from 'react'
import { Alert } from 'antd'
import jsQR from 'jsqr'

interface Props {
    onResult: (text: string) => void
}

export function QrScanner({ onResult }: Props) {
    const videoRef = useRef<HTMLVideoElement>(null)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        let stream: MediaStream | null = null
        let frame = 0
        let stopped = false
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d', { willReadFrequently: true })

        const tick = () => {
            const video = videoRef.current
            if (stopped || !video || !ctx) return
            if (video.readyState === video.HAVE_ENOUGH_DATA && video.videoWidth > 0) {
                canvas.width = video.videoWidth
                canvas.height = video.videoHeight
                ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
                const image = ctx.getImageData(0, 0, canvas.width, canvas.height)
                const code = jsQR(image.data, image.width, image.height)
                if (code?.data) {
                    stopped = true
                    onResult(code.data)
                    return
                }
            }
            frame = requestAnimationFrame(tick)
        }

        const start = async () => {
            if (!navigator.mediaDevices?.getUserMedia) {
                setError('Este navegador no permite usar la cámara aquí. Abre la app por HTTPS.')
                return
            }
            try {
                stream = await navigator.mediaDevices.getUserMedia({
                    video: { facingMode: { ideal: 'environment' } },
                    audio: false,
                })
                const video = videoRef.current
                if (!video || stopped) {
                    stream.getTracks().forEach((t) => t.stop())
                    return
                }
                video.srcObject = stream
                // iOS exige playsInline y muted para reproducir sin pantalla completa.
                await video.play()
                frame = requestAnimationFrame(tick)
            } catch {
                setError('No se pudo acceder a la cámara. Revisa el permiso en los ajustes del navegador.')
            }
        }

        void start()

        return () => {
            stopped = true
            cancelAnimationFrame(frame)
            stream?.getTracks().forEach((t) => t.stop())
        }
    }, [onResult])

    if (error) return <Alert type="error" showIcon title={error} />

    return (
        <video
            ref={videoRef}
            playsInline
            muted
            style={{ width: '100%', borderRadius: 8, background: '#000' }}
        />
    )
}