// Fichero: model/patterns/state/LobbyArmadoState.java
package com.uade.tpo.Scrims.model.patterns.state;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.uade.tpo.Scrims.model.domain.Confirmation;
import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.model.infrastructure.persistence.ConfirmationRepository;

@Component("lobbyArmadoState")
public class LobbyArmadoState implements ScrimState {
    
    @Autowired
    private ConfirmationRepository confirmationRepository;

    @Override
    public void aceptarPostulacion(Scrim scrim, User postulante) {
        throw new UnsupportedOperationException("No se pueden aceptar más jugadores, el lobby está lleno.");
    }

    @Override
    public void confirmarParticipacion(Scrim scrim, User jugador) {
        System.out.println("Jugador " + jugador.getUsername() + " intenta confirmar para el scrim " + scrim.getId());

        // Regla: Un jugador no puede confirmar dos veces
        if (confirmationRepository.existsByScrimIdAndUserId(scrim.getId(), jugador.getId())) {
            throw new IllegalStateException("Ya has confirmado tu participación.");
        }

        // Creamos y guardamos el registro de confirmación
        Confirmation confirmation = new Confirmation();
        confirmation.setScrim(scrim);
        confirmation.setUser(jugador);
        confirmationRepository.save(confirmation);

        // --- LÓGICA DE TRANSICIÓN DE ESTADO ---
        long confirmacionesActuales = confirmationRepository.countByScrimId(scrim.getId());
        // Reutilizamos la lógica para obtener el número máximo de jugadores
        int maxJugadores;
        try {
            String[] parts = scrim.getFormato().split("v");
            maxJugadores = Integer.parseInt(parts[0]) * 2;
        } catch (Exception e) {
            maxJugadores = 10; // Fallback
        }

        System.out.println("Progreso de confirmaciones: " + confirmacionesActuales + "/" + maxJugadores);

        if (confirmacionesActuales >= maxJugadores) {
            System.out.println("¡Todos han confirmado! Transicionando a CONFIRMADO.");
            scrim.setState(new ConfirmadoState());
        }
    }
}