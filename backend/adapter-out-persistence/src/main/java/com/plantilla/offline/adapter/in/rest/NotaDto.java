package com.plantilla.offline.adapter.in.rest;

import com.plantilla.offline.domain.nota.Nota;
import java.util.UUID;

public record NotaDto(UUID id, String titulo, String contenido) {

    public static NotaDto from(Nota n) {
        return new NotaDto(n.getId(), n.getTitulo(), n.getContenido());
    }
}