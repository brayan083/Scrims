
package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.Team;
import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.model.infrastructure.persistence.ScrimRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.ScrimSpecification;
import com.uade.tpo.Scrims.model.infrastructure.persistence.TeamRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.UserRepository;
import com.uade.tpo.Scrims.model.patterns.state.BuscandoJugadoresState;
import com.uade.tpo.Scrims.model.patterns.state.CanceladoState;
import com.uade.tpo.Scrims.model.patterns.state.FinalizadoState;
import com.uade.tpo.Scrims.model.patterns.state.LobbyArmadoState;
import com.uade.tpo.Scrims.model.patterns.strategy.MatchmakingStrategy;
import com.uade.tpo.Scrims.model.patterns.strategy.MatchmakingStrategyFactory;
import com.uade.tpo.Scrims.view.dto.request.CreateScrimRequest;
import com.uade.tpo.Scrims.view.dto.request.FinalizeScrimRequest;
import com.uade.tpo.Scrims.view.dto.request.PlayerStatisticDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.uade.tpo.Scrims.view.dto.response.PostulationResponseDTO;
import com.uade.tpo.Scrims.view.dto.response.ScrimDetailResponseDTO;
import com.uade.tpo.Scrims.view.dto.response.ScrimResponse;
import com.uade.tpo.Scrims.view.dto.response.StatisticResponseDTO;
import com.uade.tpo.Scrims.view.dto.response.TeamMemberResponseDTO;
import com.uade.tpo.Scrims.view.dto.response.TeamResponseDTO;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.uade.tpo.Scrims.model.domain.Estadistica;
import com.uade.tpo.Scrims.model.domain.Postulation;
import com.uade.tpo.Scrims.model.infrastructure.persistence.ConfirmationRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.EstadisticaRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.PostulationRepository;

@Service
public class ScrimService {
    @Autowired
    private ScrimRepository scrimRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostulationRepository postulationRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private BuscandoJugadoresState buscandoJugadoresState;
    @Autowired
    private LobbyArmadoState lobbyArmadoState;
    @Autowired
    private EstadisticaRepository estadisticaRepository;
    @Autowired
    private ConfirmationRepository confirmationRepository;
    @Autowired
    private MatchmakingStrategyFactory strategyFactory;

    private static final int MMR_CHANGE_ON_WIN = 5;
    private static final int MMR_CHANGE_ON_LOSS = -5;

    @Transactional
    public ScrimResponse createScrim(CreateScrimRequest request, String username) {
        User creador = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Creador no encontrado."));

        Scrim newScrim = new Scrim();
        newScrim.setJuego(request.getJuego());
        newScrim.setFormato(request.getFormato());
        newScrim.setRegion(request.getRegion());
        newScrim.setRangoMin(request.getRangoMin());
        newScrim.setRangoMax(request.getRangoMax());
        newScrim.setFechaHora(request.getFechaHora());
        newScrim.setState(new BuscandoJugadoresState());
        newScrim.setCreador(creador);

        String strategy = (request.getMatchmakingStrategy() != null && !request.getMatchmakingStrategy().isEmpty())
                ? request.getMatchmakingStrategy()
                : "MANUAL";
        newScrim.setMatchmakingStrategy(strategy);

        Scrim savedScrim = scrimRepository.save(newScrim);

        Team teamA = new Team();
        teamA.setScrim(savedScrim);
        teamA.setNombre("Equipo A");
        teamA.getMiembros().add(creador);
        teamRepository.save(teamA);

        Team teamB = new Team();
        teamB.setScrim(savedScrim);
        teamB.setNombre("Equipo B");
        teamRepository.save(teamB);

        return mapToScrimResponse(savedScrim);
    }

    public List<ScrimResponse> searchScrims(String juego, String formato, String region, String estado) {
        Specification<Scrim> spec = Specification.where(null);

        if (estado != null && !estado.isEmpty()) {
            spec = spec.and(ScrimSpecification.tieneEstado(estado));
        } else {
            spec = spec.and(ScrimSpecification.tieneEstado("BUSCANDO_JUGADORES"));
        }

        if (juego != null && !juego.isEmpty()) {
            spec = spec.and(ScrimSpecification.tieneJuego(juego));
        }
        if (formato != null && !formato.isEmpty()) {
            spec = spec.and(ScrimSpecification.tieneFormato(formato));
        }
        if (region != null && !region.isEmpty()) {
            spec = spec.and(ScrimSpecification.tieneRegion(region));
        }

        List<Scrim> scrims = scrimRepository.findAll(spec);
        return scrims.stream().map(this::mapToScrimResponse).collect(Collectors.toList());
    }

    public List<ScrimResponse> findScrimsByParticipant(String username) {
        User participant = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        List<Scrim> scrims = scrimRepository.findScrimsByParticipantId(participant.getId());

        return scrims.stream().map(this::mapToScrimResponse).collect(Collectors.toList());
    }

    public List<ScrimResponse> findScrimsByCreator(String username) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        List<Scrim> scrims = scrimRepository.findByCreadorIdOrderByFechaHoraDesc(creator.getId());

        return scrims.stream().map(this::mapToScrimResponse).collect(Collectors.toList());
    }

    public ScrimDetailResponseDTO getScrimDetails(Long scrimId) {
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado con ID: " + scrimId));

        Set<Long> confirmedUserIds = confirmationRepository.findByScrimId(scrimId).stream()
                .map(confirmation -> confirmation.getUser().getId())
                .collect(Collectors.toSet());

        List<Team> teams = teamRepository.findByScrimId(scrimId);

        ScrimDetailResponseDTO responseDTO = new ScrimDetailResponseDTO();
        responseDTO.setId(scrim.getId());
        responseDTO.setJuego(scrim.getJuego());
        responseDTO.setFormato(scrim.getFormato());
        responseDTO.setRegion(scrim.getRegion());
        responseDTO.setRangoMin(scrim.getRangoMin());
        responseDTO.setRangoMax(scrim.getRangoMax());
        responseDTO.setFechaHora(scrim.getFechaHora());
        responseDTO.setEstado(scrim.getEstado());
        responseDTO.setCreadorUsername(scrim.getCreador().getUsername());

        List<TeamResponseDTO> teamDTOs = teams.stream().map(team -> {
            TeamResponseDTO teamDTO = new TeamResponseDTO();
            teamDTO.setId(team.getId());
            teamDTO.setNombre(team.getNombre());

            List<TeamMemberResponseDTO> memberDTOs = team.getMiembros().stream().map(member -> {
                TeamMemberResponseDTO memberDTO = new TeamMemberResponseDTO();
                memberDTO.setUserId(member.getId());
                memberDTO.setUsername(member.getUsername());
                memberDTO.setUserRank(member.getRango());
                memberDTO.setHaConfirmado(confirmedUserIds.contains(member.getId()));

                return memberDTO;
            }).collect(Collectors.toList());

            teamDTO.setMiembros(memberDTOs);
            return teamDTO;
        }).collect(Collectors.toList());

        responseDTO.setTeams(teamDTOs);

        return responseDTO;
    }

    private ScrimResponse mapToScrimResponse(Scrim scrim) {
        ScrimResponse response = new ScrimResponse();
        response.setId(scrim.getId());
        response.setJuego(scrim.getJuego());
        response.setFormato(scrim.getFormato());
        response.setRegion(scrim.getRegion());
        response.setRangoMin(scrim.getRangoMin());
        response.setRangoMax(scrim.getRangoMax());
        response.setFechaHora(scrim.getFechaHora());
        response.setEstado(scrim.getEstado());
        response.setCreadorUsername(scrim.getCreador().getUsername());
        return response;
    }

    @Transactional
    public Postulation applyToScrim(Long scrimId, String username) {
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado"));
        User postulante = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (scrim.getCreador().getId().equals(postulante.getId())) {
            throw new IllegalStateException("No puedes postularte a tu propio scrim.");
        }

        if (!"BUSCANDO_JUGADORES".equals(scrim.getEstado())) {
            throw new IllegalStateException("Este scrim no acepta postulaciones actualmente.");
        }

        if (postulationRepository.existsByScrimIdAndPostulanteId(scrimId, postulante.getId())) {
            throw new IllegalStateException("Ya te has postulado a este scrim.");
        }

        MatchmakingStrategy strategy = strategyFactory.getStrategy(scrim.getMatchmakingStrategy());
        String nuevoEstado = strategy.procesarPostulacion(postulante, scrim);
        if ("RECHAZADA".equals(nuevoEstado)) {
            throw new IllegalStateException(
                    "Tu postulación fue rechazada automáticamente por la estrategia del scrim (ej. no cumples el rango).");
        }

        Postulation newPostulation = new Postulation();
        newPostulation.setScrim(scrim);
        newPostulation.setPostulante(postulante);
        newPostulation.setEstado(nuevoEstado);

        Postulation savedPostulation = postulationRepository.save(newPostulation);

        if ("ACEPTADA".equals(nuevoEstado)) {
            // Si la estrategia aceptó automáticamente al jugador (ej. ByMMRStrategy)
            // Lo añadimos al lobby inmediatamente.
            System.out.println("[ScrimService] Postulación " + savedPostulation.getId()
                    + " aceptada automáticamente. Añadiendo al lobby...");

            addPlayerToLobby(scrim, postulante);
        }
        return savedPostulation;
    }

    /**
     * Reemplaza el estado en memoria con el bean de Spring para que tenga acceso a los repositorios.
     */
    @Transactional
    public Postulation acceptPostulation(Long scrimId, Long postulationId, String creatorUsername) {

        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado con ID: " + scrimId));

        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new RuntimeException("Usuario creador no encontrado: " + creatorUsername));

        Postulation postulation = postulationRepository.findById(postulationId)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada con ID: " + postulationId));

        if (!scrim.getCreador().getId().equals(creator.getId())) {
            throw new RuntimeException("Acción no permitida: No eres el creador de este scrim.");
        }

        addPlayerToLobby(scrim, postulation.getPostulante());

        scrim.aceptarPostulacion(postulation.getPostulante());

        postulation.setEstado("ACEPTADA");
        postulationRepository.save(postulation);
        scrimRepository.save(scrim);

        return postulation;
    }

    /**
     * Añade un jugador al lobby. Reemplaza el estado en memoria con el bean de Spring.
     * No realiza chequeos de permisos.
     */
    private void addPlayerToLobby(Scrim scrim, User postulante) {
        if (scrim.getCurrentState() instanceof BuscandoJugadoresState) {
            scrim.setCurrentState(buscandoJugadoresState);
        } else {
            if (scrim.getCurrentState() == null) {
                scrim.setCurrentState(buscandoJugadoresState);
            }
        }

        scrim.aceptarPostulacion(postulante);

        scrimRepository.save(scrim);
    }

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
    }

    @Transactional
    public void finalizeScrim(Long scrimId, FinalizeScrimRequest request, String username) {

        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado con ID: " + scrimId));

        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario creador no encontrado."));

        if (!scrim.getCreador().getId().equals(creator.getId())) {
            throw new RuntimeException("Acción no permitida: No eres el creador de este scrim.");
        }

        if (!"EN_JUEGO".equals(scrim.getEstado())) {
            throw new IllegalStateException(
                    "El scrim no se puede finalizar porque no está en juego. Estado actual: " + scrim.getEstado());
        }

        System.out.println("Finalizando Scrim ID: " + scrimId + ". Resultado: " + request.getResultado());
        for (PlayerStatisticDTO statDTO : request.getPlayerStats()) {
            User player = userRepository.findById(statDTO.getUserId())
                    .orElseThrow(
                            () -> new RuntimeException("Jugador con ID " + statDTO.getUserId() + " no encontrado."));

            Estadistica estadistica = new Estadistica();
            estadistica.setScrim(scrim);
            estadistica.setUser(player);
            estadistica.setKda(statDTO.getKda());
            estadistica.setMvp(statDTO.isMvp());

            estadisticaRepository.save(estadistica);
            System.out.println("Estadística guardada para el jugador: " + player.getUsername());
        }

        List<Team> teams = teamRepository.findByScrimId(scrimId);
        if (teams.size() < 2) {
            throw new IllegalStateException(
                    "El scrim no tiene los dos equipos configurados correctamente para calcular el MMR.");
        }

        Team teamA = teams.get(0);
        Team teamB = teams.get(1);

        List<User> ganadores;
        List<User> perdedores;

        if ("VICTORIA_EQUIPO_A".equalsIgnoreCase(request.getResultado())) {
            ganadores = teamA.getMiembros();
            perdedores = teamB.getMiembros();
        } else if ("VICTORIA_EQUIPO_B".equalsIgnoreCase(request.getResultado())) {
            ganadores = teamB.getMiembros();
            perdedores = teamA.getMiembros();
        } else {
            System.out.println("Resultado es EMPATE o no reconocido. No se ajustará el MMR.");
            scrim.setState(new FinalizadoState());
            scrimRepository.save(scrim);
            return;
        }

        System.out.println("--- Actualizando MMR ---");
        for (User ganador : ganadores) {
            int nuevoRango = ganador.getRango() + MMR_CHANGE_ON_WIN;
            System.out.println(
                    "Jugador " + ganador.getUsername() + " (ganador): " + ganador.getRango() + " -> " + nuevoRango);
            ganador.setRango(nuevoRango);
            userRepository.save(ganador);
        }

        for (User perdedor : perdedores) {
            int nuevoRango = perdedor.getRango() + MMR_CHANGE_ON_LOSS;
            if (nuevoRango < 0)
                nuevoRango = 0;
            System.out.println(
                    "Jugador " + perdedor.getUsername() + " (perdedor): " + perdedor.getRango() + " -> " + nuevoRango);
            perdedor.setRango(nuevoRango);
            userRepository.save(perdedor);
        }

        scrim.setState(new FinalizadoState());
        scrimRepository.save(scrim);

        System.out.println("Scrim ID: " + scrimId + " ha sido movido al estado FINALIZADO.");
    }

    @Transactional
    public void cancelScrim(Long scrimId, String username) {
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado con ID: " + scrimId));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (!scrim.getCreador().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para cancelar este scrim.");
        }

        List<String> cancellableStates = List.of("BUSCANDO_JUGADORES", "LOBBY_ARMADO", "CONFIRMADO");
        if (!cancellableStates.contains(scrim.getEstado())) {
            throw new IllegalStateException("No se puede cancelar un scrim que ya está en juego o ha finalizado.");
        }

        scrim.setState(new CanceladoState());
        scrimRepository.save(scrim);

        System.out.println("Scrim ID: " + scrimId + " ha sido CANCELADO.");
    }

    public List<StatisticResponseDTO> getScrimStatistics(Long scrimId) {
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado con ID: " + scrimId));

        if (!"FINALIZADO".equals(scrim.getEstado())) {
            throw new IllegalStateException("Las estadísticas solo están disponibles para scrims finalizados.");
        }

        List<Estadistica> stats = estadisticaRepository.findByScrimId(scrimId);

        return stats.stream().map(stat -> {
            StatisticResponseDTO dto = new StatisticResponseDTO();
            dto.setUsername(stat.getUser().getUsername());
            dto.setKda(stat.getKda());
            dto.setMvp(stat.isMvp());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<PostulationResponseDTO> getPostulationsForScrim(Long scrimId, String username) {
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado con ID: " + scrimId));
        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (!scrim.getCreador().getId().equals(requester.getId())) {
            throw new RuntimeException("No tienes permiso para ver las postulaciones de este scrim.");
        }

        List<Postulation> postulations = postulationRepository.findByScrimId(scrimId);

        return postulations.stream().map(postulation -> {
            PostulationResponseDTO dto = new PostulationResponseDTO();
            User postulante = postulation.getPostulante();

            dto.setPostulationId(postulation.getId());
            dto.setUserId(postulante.getId());
            dto.setUsername(postulante.getUsername());
            dto.setUserRank(postulante.getRango());
            dto.setStatus(postulation.getEstado());

            return dto;
        }).collect(Collectors.toList());
    }
}