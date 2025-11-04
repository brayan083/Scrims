// Fichero: model/patterns/state/BuscandoJugadoresState.java
package com.uade.tpo.Scrims.model.patterns.state;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.Team;
import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.model.infrastructure.persistence.TeamRepository;
import com.uade.tpo.Scrims.model.patterns.observer.DomainEventBus;
import com.uade.tpo.Scrims.model.patterns.observer.ScrimStateChangedEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component("buscandoJugadoresState") // Nombramos el bean para poder inyectarlo
public class BuscandoJugadoresState implements ScrimState {

    @Autowired
    private TeamRepository teamRepository; // Necesitamos acceso a los repositorios

    @Autowired
    private DomainEventBus eventBus;
    
    @Override
    public void aceptarPostulacion(Scrim scrim, User postulante) {
        System.out.println("Aceptando jugador en estado BUSCANDO_JUGADORES...");

        List<Team> teams = teamRepository.findByScrimId(scrim.getId());
        Team teamA = teams.get(0);
        Team teamB = teams.get(1);

        // Lógica simple para añadir al equipo con menos jugadores
        if (teamA.getMiembros().size() <= teamB.getMiembros().size()) {
            teamA.getMiembros().add(postulante);
            teamRepository.save(teamA);
        } else {
            teamB.getMiembros().add(postulante);
            teamRepository.save(teamB);
        }

        int maxJugadores;
        try {
            // 1. Tomamos el formato del scrim, ej: "1v1" o "5v5"
            String formato = scrim.getFormato();
            // 2. Lo separamos por la 'v' para obtener un array ["1", "1"] o ["5", "5"]
            String[] parts = formato.split("v");
            // 3. Convertimos la primera parte a un número entero
            int playersPerTeam = Integer.parseInt(parts[0]);
            // 4. Calculamos el total de jugadores (jugadores por equipo * 2)
            maxJugadores = playersPerTeam * 2;
        } catch (Exception e) {
            // Si el formato no es válido (ej. "Deathmatch"), usamos un valor por defecto seguro.
            // En una implementación más avanzada, esto podría leerse de una configuración del juego.
            System.err.println("Formato de scrim no estándar: " + scrim.getFormato() + ". Usando 10 jugadores por defecto.");
            maxJugadores = 10;
        }
        
        int totalJugadores = teamA.getMiembros().size() + teamB.getMiembros().size();
        System.out.println("Progreso del lobby: " + totalJugadores + "/" + maxJugadores);
    
        if (totalJugadores >= maxJugadores) {
            System.out.println("¡Lobby lleno! Transicionando a LOBBY_ARMADO.");
            LobbyArmadoState nuevoEstado = new LobbyArmadoState();
            scrim.setState(nuevoEstado);

            // --- ¡PUBLICAMOS EL EVENTO! ---
            eventBus.publish(new ScrimStateChangedEvent(scrim, "LOBBY_ARMADO"));
        }
    }

    @Override
    public void confirmarParticipacion(Scrim scrim, User jugador) {
        throw new UnsupportedOperationException("No se puede confirmar participación mientras se buscan jugadores.");
    }
}