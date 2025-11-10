package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.Team;
import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.model.infrastructure.persistence.ScrimRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.TeamRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.UserRepository;
import com.uade.tpo.Scrims.model.patterns.state.FinalizadoState;
import com.uade.tpo.Scrims.model.patterns.state.LobbyArmadoState;
import com.uade.tpo.Scrims.view.dto.request.FinalizeScrimRequest;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for finalizing scrims and managing MMR calculations.
 * Handles confirmation of participation, finalization logic, and player ranking updates.
 */
@Service
public class ScrimFinalizationService {
    private static final Logger log = LoggerFactory.getLogger(ScrimFinalizationService.class);

    private static final int MMR_CHANGE_ON_WIN = 5;
    private static final int MMR_CHANGE_ON_LOSS = -5;

    @Autowired
    private ScrimRepository scrimRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private LobbyArmadoState lobbyArmadoState;
    @Autowired
    private StatisticsService statisticsService;

    /**
     * Confirms a player's participation in a scrim.
     * Updates the scrim state and records the confirmation.
     *
     * @param scrimId the ID of the scrim
     * @param username the username of the player confirming
     * @throws RuntimeException if scrim or user not found
     */
    @Transactional
    public void confirmParticipation(Long scrimId, String username) {
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado"));
        User jugador = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (scrim.getCurrentState() instanceof LobbyArmadoState) {
            scrim.setCurrentState(lobbyArmadoState);
        }

        scrim.confirmarParticipacion(jugador);
        scrimRepository.save(scrim);
        
        log.info("Jugador {} confirmó participación en scrim {}", username, scrimId);
    }

    /**
     * Finalizes a scrim by saving statistics and updating player MMR.
     * Only the scrim creator can finalize, and only scrims in EN_JUEGO state.
     *
     * @param scrimId the ID of the scrim
     * @param request the finalization request with result and player stats
     * @param username the username of the scrim creator
     * @throws RuntimeException if scrim or user not found, or user is not the creator
     * @throws IllegalStateException if scrim is not in EN_JUEGO state or teams are not properly configured
     */
    @Transactional
    public void finalizeScrim(Long scrimId, FinalizeScrimRequest request, String username) {
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado con ID: " + scrimId));

        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario creador no encontrado."));

        validateFinalizationPermissions(scrim, creator);

        log.info("Finalizando Scrim ID: {}. Resultado: {}", scrimId, request.getResultado());

        // Save player statistics
        statisticsService.savePlayerStatistics(scrim, request.getPlayerStats());

        // Update MMR based on result
        updatePlayerMMR(scrim, request.getResultado());

        // Mark scrim as finalized
        scrim.setState(new FinalizadoState());
        scrimRepository.save(scrim);

        log.info("Scrim ID: {} ha sido movido al estado FINALIZADO.", scrimId);
    }

    /**
     * Updates player MMR based on the scrim result.
     * Winners gain MMR, losers lose MMR (with a minimum of 0).
     *
     * @param scrim the Scrim entity
     * @param resultado the match result (VICTORIA_EQUIPO_A, VICTORIA_EQUIPO_B, or draw)
     */
    private void updatePlayerMMR(Scrim scrim, String resultado) {
        List<Team> teams = teamRepository.findByScrimId(scrim.getId());
        if (teams.size() < 2) {
            throw new IllegalStateException(
                    "El scrim no tiene los dos equipos configurados correctamente para calcular el MMR.");
        }

        Team teamA = teams.get(0);
        Team teamB = teams.get(1);

        List<User> ganadores;
        List<User> perdedores;

        if ("VICTORIA_EQUIPO_A".equalsIgnoreCase(resultado)) {
            ganadores = teamA.getMiembros();
            perdedores = teamB.getMiembros();
        } else if ("VICTORIA_EQUIPO_B".equalsIgnoreCase(resultado)) {
            ganadores = teamB.getMiembros();
            perdedores = teamA.getMiembros();
        } else {
            log.info("Resultado es EMPATE o no reconocido. No se ajustará el MMR.");
            return;
        }

        log.info("Actualizando MMR para Scrim ID: {}", scrim.getId());
        
        updateMMRForPlayers(ganadores, MMR_CHANGE_ON_WIN, "ganador");
        updateMMRForPlayers(perdedores, MMR_CHANGE_ON_LOSS, "perdedor");
    }

    /**
     * Updates MMR for a list of players.
     *
     * @param players the list of players to update
     * @param mmrChange the amount of MMR to add (positive) or subtract (negative)
     * @param role the role label for logging (e.g., "ganador" or "perdedor")
     */
    private void updateMMRForPlayers(List<User> players, int mmrChange, String role) {
        for (User player : players) {
            int nuevoRango = player.getRango() + mmrChange;
            if (nuevoRango < 0) {
                nuevoRango = 0;
            }
            log.debug("Jugador {} ({}): {} -> {}", player.getUsername(), role, player.getRango(), nuevoRango);
            player.setRango(nuevoRango);
            userRepository.save(player);
        }
    }

    /**
     * Validates if a user can finalize a scrim.
     *
     * @param scrim the Scrim entity
     * @param creator the User attempting to finalize
     * @throws RuntimeException if user is not the creator
     * @throws IllegalStateException if scrim is not in EN_JUEGO state
     */
    private void validateFinalizationPermissions(Scrim scrim, User creator) {
        if (!scrim.getCreador().getId().equals(creator.getId())) {
            throw new RuntimeException("Acción no permitida: No eres el creador de este scrim.");
        }

        if (!"EN_JUEGO".equals(scrim.getEstado())) {
            throw new IllegalStateException(
                    "El scrim no se puede finalizar porque no está en juego. Estado actual: " + scrim.getEstado());
        }
    }
}
