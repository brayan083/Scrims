// Fichero: model/domain/Confirmation.java
package com.uade.tpo.Scrims.model.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "confirmations")
@Data
public class Confirmation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "scrim_id", nullable = false)
    private Scrim scrim;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}