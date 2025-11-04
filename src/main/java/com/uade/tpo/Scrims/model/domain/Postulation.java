// Fichero: model/domain/Postulation.java
package com.uade.tpo.Scrims.model.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "postulations")
@Data
public class Postulation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "scrim_id", nullable = false)
    private Scrim scrim;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User postulante;

    private String estado; // "PENDIENTE", "ACEPTADA", "RECHAZADA"
}