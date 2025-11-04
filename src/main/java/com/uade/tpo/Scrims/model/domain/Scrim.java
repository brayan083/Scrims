// Fichero: model/domain/Scrim.java
package com.uade.tpo.Scrims.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uade.tpo.Scrims.model.patterns.state.BuscandoJugadoresState;
import com.uade.tpo.Scrims.model.patterns.state.LobbyArmadoState;
import com.uade.tpo.Scrims.model.patterns.state.ScrimState;

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

    @Transient // Le decimos a JPA que no guarde este campo en la BBDD
    @JsonIgnore // Ignoramos el campo en las respuestas JSON para evitar recursividad
    private ScrimState currentState;

    @ManyToOne // Un usuario puede crear muchos scrims
    @JoinColumn(name = "creador_id", nullable = false)
    private User creador;

    // Este método se ejecuta automáticamente después de que un Scrim se carga de la
    // BBDD
    @PostLoad
    private void initState() {
        if ("LOBBY_ARMADO".equals(this.estado)) {
            this.currentState = new LobbyArmadoState();
        } else {
            // Estado por defecto
            this.currentState = new BuscandoJugadoresState();
        }
    }

    // Método para cambiar de estado de forma segura
    public void setState(ScrimState newState) {
        this.currentState = newState;

        // Convertimos de CamelCase a SNAKE_CASE para la persistencia.
        // Ej: "BuscandoJugadoresState" -> "BuscandoJugadores" -> "BUSCANDO_JUGADORES"
        String stateName = newState.getClass().getSimpleName().replace("State", "");
        String snakeCaseState = stateName.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();

        this.estado = snakeCaseState;
    }

    // --- Métodos que delegan el comportamiento al estado actual ---
    public void aceptarPostulacion(User postulante) {
        this.currentState.aceptarPostulacion(this, postulante);
    }

    public void confirmarParticipacion(User jugador) {
        this.currentState.confirmarParticipacion(this, jugador);
    }

}