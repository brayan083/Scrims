package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.Team;
import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.model.infrastructure.persistence.ScrimRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.ScrimSpecification;
import com.uade.tpo.Scrims.model.infrastructure.persistence.TeamRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.UserRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.ConfirmationRepository;
import com.uade.tpo.Scrims.model.patterns.state.BuscandoJugadoresState;
import com.uade.tpo.Scrims.model.patterns.state.CanceladoState;
import com.uade.tpo.Scrims.view.dto.request.CreateScrimRequest;
import com.uade.tpo.Scrims.view.mapper.ScrimMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.uade.tpo.Scrims.view.dto.response.ScrimDetailResponseDTO;
import com.uade.tpo.Scrims.view.dto.response.ScrimResponse;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Core service for Scrim lifecycle management and queries.
 * Handles scrim creation, search, retrieval, and cancellation.
 * For complex operations involving multiple services, use ScrimFacade instead.
 */
@Service
public class ScrimService {
    private static final Logger log = LoggerFactory.getLogger(ScrimService.class);
    
    @Autowired
    private ScrimRepository scrimRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private ConfirmationRepository confirmationRepository;
    @Autowired
    private ScrimMapper scrimMapper;

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

        return scrimMapper.toScrimResponse(savedScrim);
    }

    public List<ScrimResponse> searchScrims(String juego, String formato, String region, String estado) {
    Specification<Scrim> spec = ScrimSpecification.empty();

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
    return scrims.stream()
            .map(scrimMapper::toScrimResponse)
            .collect(Collectors.toList());
}

    public List<ScrimResponse> findScrimsByParticipant(String username) {
        User participant = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        List<Scrim> scrims = scrimRepository.findScrimsByParticipantId(participant.getId());

        return scrims.stream()
                .map(scrimMapper::toScrimResponse)
                .collect(Collectors.toList());
    }

    public List<ScrimResponse> findScrimsByCreator(String username) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        List<Scrim> scrims = scrimRepository.findByCreadorIdOrderByFechaHoraDesc(creator.getId());

        return scrims.stream()
                .map(scrimMapper::toScrimResponse)
                .collect(Collectors.toList());
    }

    public ScrimDetailResponseDTO getScrimDetails(Long scrimId) {
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado con ID: " + scrimId));

        Set<Long> confirmedUserIds = confirmationRepository.findByScrimId(scrimId).stream()
                .map(confirmation -> confirmation.getUser().getId())
                .collect(Collectors.toSet());

        List<Team> teams = teamRepository.findByScrimId(scrimId);

        return scrimMapper.toScrimDetailResponse(scrim, teams, confirmedUserIds);
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

        log.info("Scrim ID: {} ha sido CANCELADO.", scrimId);
    }
}