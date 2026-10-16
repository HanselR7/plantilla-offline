package com.plantilla.offline.application.nota;

import com.plantilla.offline.domain.nota.Nota;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotaUseCase {
    Nota crear(String titulo, String contenido);
    Optional<Nota> actualizar(UUID id, String titulo, String contenido);
    Optional<Nota> obtener(UUID id);
    List<Nota> listar();
    boolean eliminar(UUID id);
}