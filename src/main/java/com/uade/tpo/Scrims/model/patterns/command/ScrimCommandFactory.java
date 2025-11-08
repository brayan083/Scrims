package com.uade.tpo.Scrims.model.patterns.command;

import com.uade.tpo.Scrims.model.infrastructure.persistence.TeamRepository;
import com.uade.tpo.Scrims.view.dto.request.CommandRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory para crear comandos de Scrim e inyectar sus dependencias.
 */
@Component
public class ScrimCommandFactory {

    @Autowired
    private TeamRepository teamRepository;

    public ScrimCommand createCommand(CommandRequestDTO request, Long scrimId) {
        String commandName = request.getCommandName();

        if (commandName == null || commandName.isEmpty()) {
            throw new IllegalArgumentException("El nombre del comando (commandName) no puede ser nulo o vacío.");
        }

        switch (commandName.toUpperCase()) {
            case "SWAP_PLAYERS":
                return new SwapPlayersCommand(
                        scrimId,
                        request.getPayload(),
                        teamRepository
                );

            // case "KICK_PLAYER":
            //     return new KickPlayerCommand(scrimId, request.getPayload(), teamRepository, userRepository);

            default:
                throw new IllegalArgumentException("Comando no reconocido: " + commandName);
        }
    }
}
