// Fichero: model/domain/Scrim.java
package com.uade.tpo.Scrims.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "scrims")
@Data
public class Scrim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String juego;
    private String formato; // "5v5", "1v1", etc.
    private String region;
    private Integer rangoMin;
    private Integer rangoMax;
    private LocalDateTime fechaHora;
    private String estado; // "BUSCANDO_JUGADORES", "COMPLETO", etc.

    @ManyToOne // Un usuario puede crear muchos scrims
    @JoinColumn(name = "creador_id", nullable = false)
    private User creador;
}