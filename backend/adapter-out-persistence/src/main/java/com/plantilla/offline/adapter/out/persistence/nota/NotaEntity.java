package com.plantilla.offline.adapter.out.persistence.nota;

import com.plantilla.offline.adapter.out.persistence.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "nota")
@Getter
@Setter
public class NotaEntity extends BaseEntity {

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "contenido")
    private String contenido;
}