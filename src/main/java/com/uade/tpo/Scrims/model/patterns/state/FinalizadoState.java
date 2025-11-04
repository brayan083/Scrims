// Fichero: model/patterns/state/FinalizadoState.java
package com.uade.tpo.Scrims.model.patterns.state;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.User;

public class FinalizadoState implements ScrimState {
    @Override
    public void aceptarPostulacion(Scrim scrim, User postulante) {
        throw new UnsupportedOperationException("Esta partida ya ha finalizado.");
    }

    @Override
    public void confirmarParticipacion(Scrim scrim, User jugador) {
        throw new UnsupportedOperationException("Esta partida ya ha finalizado.");
    }
}