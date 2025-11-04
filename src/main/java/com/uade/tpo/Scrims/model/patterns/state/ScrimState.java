// Fichero: model/patterns/state/ScrimState.java
package com.uade.tpo.Scrims.model.patterns.state;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.User;

// La interfaz que define las acciones que pueden cambiar el estado de un Scrim.
public interface ScrimState {
    void aceptarPostulacion(Scrim scrim, User postulante);
    void confirmarParticipacion(Scrim scrim, User jugador);
    // ... aquí irían otros métodos como iniciarPartida(), finalizar(), cancelar(), etc.
}