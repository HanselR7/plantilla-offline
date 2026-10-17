import { useEffect } from 'react'
import { Form, Input, Modal } from 'antd'
import type { Nota, NotaInput } from './types'

interface Props {
    open: boolean
    nota: Nota | null
    loading: boolean
    onCancel: () => void
    onSubmit: (data: NotaInput) => void
}

export function NotaFormModal({ open, nota, loading, onCancel, onSubmit }: Props) {
    const [form] = Form.useForm<NotaInput>()

    useEffect(() => {
        if (open) form.setFieldsValue({ titulo: nota?.titulo ?? '', contenido: nota?.contenido ?? '' })
    }, [open, nota, form])

    return (
        <Modal
            title={nota ? 'Editar nota' : 'Nueva nota'}
            open={open}
            confirmLoading={loading}
            onCancel={onCancel}
            onOk={() => form.validateFields().then(onSubmit)}
            okText="Guardar"
            cancelText="Cancelar"
            destroyOnHidden
        >
            <Form form={form} layout="vertical">
                <Form.Item name="titulo" label="Título" rules={[{ required: true, message: 'El título es obligatorio' }]}>
                    <Input maxLength={200} />
                </Form.Item>
                <Form.Item name="contenido" label="Contenido">
                    <Input.TextArea rows={4} />
                </Form.Item>
            </Form>
        </Modal>
    )
}