package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.model.infrastructure.persistence.ScrimRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.UserRepository;
import com.uade.tpo.Scrims.model.patterns.command.ScrimCommand;
import com.uade.tpo.Scrims.model.patterns.command.ScrimCommandFactory;
import com.uade.tpo.Scrims.model.patterns.state.BuscandoJugadoresState;
import com.uade.tpo.Scrims.view.dto.request.CommandRequestDTO;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for managing team operations within scrims.
 * Handles adding players to lobby and executing team management commands.
 */
@Service
public class TeamManagementService {
    private static final Logger log = LoggerFactory.getLogger(TeamManagementService.class);

    @Autowired
    private ScrimRepository scrimRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BuscandoJugadoresState buscandoJugadoresState;
    @Autowired
    private ScrimCommandFactory commandFactory;

    /**
     * Adds a player to the scrim lobby.
     * Replaces the in-memory state with the Spring bean to ensure repository access.
     * Does not perform permission checks.
     *
     * @param scrim the Scrim entity
     * @param player the User to add to the lobby
     */
    @Transactional
    public void addPlayerToLobby(Scrim scrim, User player) {
        // Ensure the state has access to repositories by using the Spring bean
        if (scrim.getCurrentState() instanceof BuscandoJugadoresState) {
            scrim.setCurrentState(buscandoJugadoresState);
        } else {
            if (scrim.getCurrentState() == null) {
                scrim.setCurrentState(buscandoJugadoresState);
            }
        }

        scrim.aceptarPostulacion(player);
        scrimRepository.save(scrim);
        
        log.info("Jugador {} añadido al lobby del scrim {}", player.getUsername(), scrim.getId());
    }

    /**
     * Executes a team management command (e.g., move player, swap teams).
     * Only the scrim creator can execute commands, and only in modifiable states.
     *
     * @param scrimId the ID of the scrim
     * @param request the command request
     * @param username the username of the command executor
     * @return the result of the command execution
     * @throws RuntimeException if scrim or user not found, or user is not the creator
     * @throws IllegalStateException if scrim is not in a modifiable state
     */
    @Transactional
    public Object executeCommand(Long scrimId, CommandRequestDTO request, String username) {
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado con ID: " + scrimId));
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario creador no encontrado."));

        validateCommandPermissions(scrim, creator);

        ScrimCommand command = commandFactory.createCommand(request, scrimId);

        try {
            Object result = command.execute();
            log.info("Comando ejecutado exitosamente en scrim {}", scrimId);
            return result;
        } catch (Exception e) {
            log.error("Error ejecutando comando en scrim {}: {}", scrimId, e.getMessage());
            throw new RuntimeException("Error al ejecutar el comando: " + e.getMessage(), e);
        }
    }

    /**
     * Validates if a user has permission to execute commands on a scrim.
     *
     * @param scrim the Scrim entity
     * @param creator the User attempting to execute the command
     * @throws RuntimeException if user is not the creator
     * @throws IllegalStateException if scrim is not in a modifiable state
     */
    private void validateCommandPermissions(Scrim scrim, User creator) {
        if (!scrim.getCreador().getId().equals(creator.getId())) {
            throw new RuntimeException("Acción no permitida: No eres el creador de este scrim.");
        }

        List<String> modifiableStates = List.of("BUSCANDO_JUGADORES", "LOBBY_ARMADO");
        if (!modifiableStates.contains(scrim.getEstado())) {
            throw new IllegalStateException(
                    "No se pueden ejecutar comandos de gestión de equipo en un scrim que ya está en juego, finalizado o cancelado.");
        }
    }
}
