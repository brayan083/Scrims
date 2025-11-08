package com.uade.tpo.Scrims.model.patterns.command;

import com.uade.tpo.Scrims.model.domain.Team;
import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.model.infrastructure.persistence.TeamRepository;

import java.util.List;
import java.util.Map;

/**
 * Comando para intercambiar dos jugadores entre equipos de un scrim.
 */
public class SwapPlayersCommand implements ScrimCommand {

    private final Long scrimId;
    private final Map<String, Object> payload;
    private final TeamRepository teamRepository;

    public SwapPlayersCommand(Long scrimId, Map<String, Object> payload, TeamRepository teamRepository) {
        this.scrimId = scrimId;
        this.payload = payload;
        this.teamRepository = teamRepository;
    }

    @Override
    public Object execute() {
        return swapPlayers();
    }

    @Override
    public Object undo() {
        // Deshacer un swap es volver a intercambiar
        System.out.println("Deshaciendo el intercambio de jugadores...");
        return swapPlayers();
    }

    private String swapPlayers() {
        Long userId1 = getLongFromPayload("userId1");
        Long userId2 = getLongFromPayload("userId2");

        List<Team> teams = teamRepository.findByScrimId(scrimId);
        if (teams == null || teams.size() < 2) {
            throw new IllegalStateException("El Scrim no tiene los dos equipos configurados.");
        }

        User user1 = findUserInTeams(teams, userId1);
        User user2 = findUserInTeams(teams, userId2);

        if (user1 == null || user2 == null) {
            throw new RuntimeException("No se encontró a uno o ambos jugadores (ID: " + userId1 + ", " + userId2 + ") en este Scrim.");
        }

        Team team1 = findTeamOfUser(teams, user1);
        Team team2 = findTeamOfUser(teams, user2);

        if (team1.equals(team2)) {
            throw new IllegalStateException("Ambos jugadores (ID: " + userId1 + ", " + userId2 + ") ya están en el mismo equipo.");
        }

        team1.getMiembros().remove(user1);
        team1.getMiembros().add(user2);

        team2.getMiembros().remove(user2);
        team2.getMiembros().add(user1);

        teamRepository.save(team1);
        teamRepository.save(team2);

        String successMessage = "Jugadores '" + user1.getUsername() + "' y '" + user2.getUsername() + "' han sido intercambiados de equipo.";
        System.out.println("[Command.SwapPlayers] " + successMessage);
        return successMessage;
    }

    private User findUserInTeams(List<Team> teams, Long userId) {
        for (Team team : teams) {
            for (User member : team.getMiembros()) {
                if (member.getId().equals(userId)) {
                    return member;
                }
            }
        }
        return null;
    }

    private Team findTeamOfUser(List<Team> teams, User user) {
        for (Team team : teams) {
            if (team.getMiembros().contains(user)) {
                return team;
            }
        }
        return null;
    }

    /**
     * Maneja la conversión de Integer/Long desde JSON deserializado.
     */
    private Long getLongFromPayload(String key) {
        Object value = payload.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Payload inválido: falta la clave '" + key + "'.");
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof Long) {
            return (Long) value;
        }
        throw new IllegalArgumentException("Payload inválido: '" + key + "' no es un número (ID de usuario).");
    }
}