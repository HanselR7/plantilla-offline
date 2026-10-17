import { useState } from 'react'
import { Alert, Button, Popconfirm, Space, Table, Typography, message } from 'antd'
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons'
import { useConnectionStore } from '../../core/connectivity/useConnectionStore'
import { NotaFormModal } from './NotaFormModal'
import { useEliminarNota, useGuardarNota, useNotas } from './useNotas'
import type { Nota, NotaInput } from './types'
import './NotasPage.scss'

export function NotasPage() {
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
                onSuccess: () => {
                    setModalOpen(false)
                    void message.success('Nota guardada')
                },
                onError: (e) => void message.error(e.message),
            },
        )

    return (
        <div className="notas-page">
            <div className="notas-page__header">
                <Typography.Title level={3} style={{ margin: 0 }}>Notas</Typography.Title>
                <Button type="primary" icon={<PlusOutlined />} disabled={mode === 'offline'} onClick={() => abrir(null)}>
                    Nueva nota
                </Button>
            </div>

            {mode === 'offline' && (
                <Alert type="warning" showIcon style={{ marginBottom: 16 }} message="Sin conexión: no se pueden cargar ni guardar notas todavía." />
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