// Fichero: model/patterns/state/EnJuegoState.java
package com.uade.tpo.Scrims.model.patterns.state;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.User;

public class EnJuegoState implements ScrimState {
    @Override
    public void aceptarPostulacion(Scrim scrim, User postulante) {
        throw new UnsupportedOperationException("La partida ya está en juego.");
    }

    @Override
    public void confirmarParticipacion(Scrim scrim, User jugador) {
        throw new UnsupportedOperationException("La partida ya está en juego.");
    }
    
    // Podríamos añadir un método finalizarPartida() aquí
}