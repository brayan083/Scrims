// Fichero: model/patterns/state/LobbyArmadoState.java
package com.uade.tpo.Scrims.model.patterns.state;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.User;

public class LobbyArmadoState implements ScrimState {
    @Override
    public void aceptarPostulacion(Scrim scrim, User postulante) {
        throw new UnsupportedOperationException("No se pueden aceptar más jugadores, el lobby está lleno.");
    }

    @Override
    public void confirmarParticipacion(Scrim scrim, User jugador) {
        System.out.println("Jugador " + jugador.getUsername() + " ha confirmado su participación.");
        // TODO: Lógica para registrar la confirmación.
        // Si todos confirman, transicionar a CONFIRMADO.
    }
}