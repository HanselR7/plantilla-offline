package com.plantilla.offline.domain.nota;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nota {
    private UUID id;
    private String titulo;
    private String contenido;
}