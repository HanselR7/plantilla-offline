export interface Nota {
    id: string
    titulo: string
    contenido: string | null
}

export type NotaInput = Pick<Nota, 'titulo' | 'contenido'>