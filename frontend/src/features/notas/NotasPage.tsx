import { useState } from 'react'
import { Alert, Button, Popconfirm, Space, Table, Typography } from 'antd'
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons'
import { useConnectionStore } from '../../core/connectivity/useConnectionStore'
import { NotaFormModal } from './NotaFormModal'
import { useEliminarNota, useGuardarNota, useNotas } from './useNotas'
import type { Nota, NotaInput } from './types'
import './NotasPage.scss'
import { isQueued } from '../../core/http/httpClient'
import useApp from 'antd/es/app/useApp'

export function NotasPage() {
    const { message } = useApp();
    const mode = useConnectionStore((s) => s.mode)
    const { data, isLoading } = useNotas()
    const guardar = useGuardarNota()
    const eliminar = useEliminarNota()

    const [modalOpen, setModalOpen] = useState(false)
    const [editando, setEditando] = useState<Nota | null>(null)

    const abrir = (nota: Nota | null) => {
        setEditando(nota)
        setModalOpen(true)
    }

    const onSubmit = (values: NotaInput) =>
        guardar.mutate(
            { id: editando?.id, data: values },
            {
                onSuccess: (result) => {
                    setModalOpen(false)
                    void message.success(isQueued(result) ? 'Guardado sin conexión; se enviará al reconectar' : 'Nota guardada')
                },
                onError: (e) => void message.error(e.message),
            },
        )

    return (
        <div className="notas-page">
            <div className="notas-page__header">
                <Typography.Title level={3} style={{ margin: 0 }}>Notas</Typography.Title>
                <Button type="primary" icon={<PlusOutlined />} onClick={() => abrir(null)}>
                    Nueva nota
                </Button>
            </div>

            {mode === 'offline' && (
                <Alert
                    type="info"
                    showIcon
                    style={{ marginBottom: 16 }}
                    title="Sin conexión: los cambios se guardan en este dispositivo y se enviarán al reconectar."
                />
            )}

            <Table<Nota>
                rowKey="id"
                loading={isLoading}
                dataSource={data ?? []}
                pagination={false}
                columns={[
                    { title: 'Título', dataIndex: 'titulo' },
                    { title: 'Contenido', dataIndex: 'contenido', ellipsis: true },
                    {
                        title: '',
                        width: 100,
                        render: (_, nota) => (
                            <Space>
                                <Button type="text" icon={<EditOutlined />} onClick={() => abrir(nota)} />
                                <Popconfirm title="¿Eliminar esta nota?" onConfirm={() => eliminar.mutate(nota.id)}>
                                    <Button type="text" danger icon={<DeleteOutlined />} />
                                </Popconfirm>
                            </Space>
                        ),
                    },
                ]}
            />

            <NotaFormModal
                open={modalOpen}
                nota={editando}
                loading={guardar.isPending}
                onCancel={() => setModalOpen(false)}
                onSubmit={onSubmit}
            />
        </div>
    )
}