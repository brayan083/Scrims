// Fichero: model/patterns/state/ConfirmadoState.java
package com.uade.tpo.Scrims.model.patterns.state;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.User;

public class ConfirmadoState implements ScrimState {
    @Override
    public void aceptarPostulacion(Scrim scrim, User postulante) {
        throw new UnsupportedOperationException("El scrim ya está confirmado, no se pueden aceptar más jugadores.");
    }

    @Override
    public void confirmarParticipacion(Scrim scrim, User jugador) {
        throw new UnsupportedOperationException("El scrim ya está confirmado, no se puede confirmar de nuevo.");
    }
    // TODO: Aquí iría la lógica para transicionar a "EN_JUEGO" cuando llegue la fecha/hora.
}