package com.plantilla.offline.application.nota;

import com.plantilla.offline.domain.nota.Nota;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de salida: lo que el caso de uso necesita de la persistencia. */
public interface NotaRepository {
    Nota save(Nota nota);
    Optional<Nota> findById(UUID id);
    List<Nota> findAll();
    boolean softDelete(UUID id);
}