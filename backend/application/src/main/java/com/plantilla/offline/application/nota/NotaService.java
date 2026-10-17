package com.plantilla.offline.application.nota;

import com.plantilla.offline.domain.nota.Nota;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NotaService implements NotaUseCase {

    private final NotaRepository repository;

    public NotaService(NotaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Nota crear(UUID id, String titulo, String contenido) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }
        // Si el cliente ya envió esa nota antes (reintento), se devuelve la existente.
        if (id != null) {
            Optional<Nota> existente = repository.findById(id);
            if (existente.isPresent()) {
                return existente.get();
            }
        }
        return repository.save(new Nota(id, titulo.trim(), contenido));
    }

    @Override
    public Optional<Nota> actualizar(UUID id, String titulo, String contenido) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }
        return repository.findById(id).map(n -> {
            n.setTitulo(titulo.trim());
            n.setContenido(contenido);
            return repository.save(n);
        });
    }

    @Override
    public Optional<Nota> obtener(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<Nota> listar() {
        return repository.findAll();
    }

    @Override
    public boolean eliminar(UUID id) {
        return repository.softDelete(id);
    }
}