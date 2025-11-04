// Fichero: model/domain/Estadistica.java
package com.uade.tpo.Scrims.model.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "statistics")
@Data
public class Estadistica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Scrim scrim;

    @ManyToOne
    private User user;

    private boolean mvp;
    private String kda; // e.g., "15/5/10"
    // Podríamos añadir más campos como resultado (victoria/derrota), etc.
}