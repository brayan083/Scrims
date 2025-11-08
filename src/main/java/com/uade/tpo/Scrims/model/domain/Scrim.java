// Fichero: model/domain/Scrim.java
package com.uade.tpo.Scrims.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uade.tpo.Scrims.model.patterns.state.BuscandoJugadoresState;
import com.uade.tpo.Scrims.model.patterns.state.ConfirmadoState;
import com.uade.tpo.Scrims.model.patterns.state.EnJuegoState;
import com.uade.tpo.Scrims.model.patterns.state.FinalizadoState;
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
    private String formato;
    private String region;
    private Integer rangoMin;
    private Integer rangoMax;
    private LocalDateTime fechaHora;
    private String estado;
    private String matchmakingStrategy;

    @Transient
    @JsonIgnore
    private ScrimState currentState;

    @ManyToOne
    @JoinColumn(name = "creador_id", nullable = false)
    private User creador;

    @PostLoad
    private void initState() {
        if ("LOBBY_ARMADO".equals(this.estado)) {
            this.currentState = new LobbyArmadoState();
        } else if ("CONFIRMADO".equals(this.estado)) {
            this.currentState = new ConfirmadoState();
        } else if ("EN_JUEGO".equals(this.estado)) {
            this.currentState = new EnJuegoState();
        } else if ("FINALIZADO".equals(this.estado)) {
            this.currentState = new FinalizadoState();
        } else {
            this.currentState = new BuscandoJugadoresState();
        }
    }

    /**
     * Sincroniza el estado en memoria (State pattern) con el campo persistente.
     * Convierte CamelCase a SNAKE_CASE (ej: BuscandoJugadoresState -> BUSCANDO_JUGADORES).
     */
    public void setState(ScrimState newState) {
        this.currentState = newState;

        String stateName = newState.getClass().getSimpleName().replace("State", "");
        String snakeCaseState = stateName.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();

        this.estado = snakeCaseState;
    }

    public void aceptarPostulacion(User postulante) {
        this.currentState.aceptarPostulacion(this, postulante);
    }

    public void confirmarParticipacion(User jugador) {
        this.currentState.confirmarParticipacion(this, jugador);
    }

}