// Fichero: model/patterns/state/CanceladoState.java
package com.uade.tpo.Scrims.model.patterns.state;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.User;

public class CanceladoState implements ScrimState {
    // En un estado terminal, ninguna acción es válida.
    @Override
    public void aceptarPostulacion(Scrim scrim, User postulante) {
        throw new UnsupportedOperationException("Este scrim ha sido cancelado.");
    }

    @Override
    public void confirmarParticipacion(Scrim scrim, User jugador) {
        throw new UnsupportedOperationException("Este scrim ha sido cancelado.");
    }
}