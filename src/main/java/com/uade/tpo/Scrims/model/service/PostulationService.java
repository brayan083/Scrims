package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.domain.Postulation;
import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.model.infrastructure.persistence.PostulationRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.ScrimRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.UserRepository;
import com.uade.tpo.Scrims.model.patterns.strategy.MatchmakingStrategy;
import com.uade.tpo.Scrims.model.patterns.strategy.MatchmakingStrategyFactory;
import com.uade.tpo.Scrims.view.dto.response.PostulationResponseDTO;
import com.uade.tpo.Scrims.view.mapper.ScrimMapper;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for managing postulations (applications) to scrims.
 * Handles the application process, acceptance, and listing of postulations.
 */
@Service
public class PostulationService {
    private static final Logger log = LoggerFactory.getLogger(PostulationService.class);

    @Autowired
    private ScrimRepository scrimRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostulationRepository postulationRepository;
    @Autowired
    private MatchmakingStrategyFactory strategyFactory;
    @Autowired
    private ScrimMapper scrimMapper;
    @Autowired
    private TeamManagementService teamManagementService;

    /**
     * Applies a user to a scrim, following the matchmaking strategy rules.
     * If automatically accepted, adds the player to the lobby immediately.
     *
     * @param scrimId the ID of the scrim
     * @param username the username of the applicant
     * @return the created Postulation
     * @throws RuntimeException if scrim or user not found
     * @throws IllegalStateException if application is not allowed
     */
    @Transactional
    public Postulation applyToScrim(Long scrimId, String username) {
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado"));
        User postulante = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        validatePostulationEligibility(scrim, postulante);

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
            log.info("Postulación {} aceptada automáticamente. Añadiendo al lobby...", savedPostulation.getId());
            teamManagementService.addPlayerToLobby(scrim, postulante);
        }
        
        return savedPostulation;
    }

    /**
     * Accepts a postulation and adds the player to the scrim lobby.
     * Only the scrim creator can accept postulations.
     *
     * @param scrimId the ID of the scrim
     * @param postulationId the ID of the postulation to accept
     * @param creatorUsername the username of the scrim creator
     * @return the accepted Postulation
     * @throws RuntimeException if entities not found or user is not the creator
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

        teamManagementService.addPlayerToLobby(scrim, postulation.getPostulante());

        postulation.setEstado("ACEPTADA");
        postulationRepository.save(postulation);
        scrimRepository.save(scrim);

        log.info("Postulación {} aceptada manualmente por el creador.", postulationId);
        return postulation;
    }

    /**
     * Retrieves all postulations for a specific scrim.
     * Only the scrim creator can view postulations.
     *
     * @param scrimId the ID of the scrim
     * @param username the username of the requester
     * @return list of PostulationResponseDTO
     * @throws RuntimeException if scrim or user not found, or user is not the creator
     */
    public List<PostulationResponseDTO> getPostulationsForScrim(Long scrimId, String username) {
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado con ID: " + scrimId));
        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        if (!scrim.getCreador().getId().equals(requester.getId())) {
            throw new RuntimeException("No tienes permiso para ver las postulaciones de este scrim.");
        }

        List<Postulation> postulations = postulationRepository.findByScrimId(scrimId);

        return postulations.stream()
                .map(scrimMapper::toPostulationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validates if a user can apply to a scrim.
     * Checks if user is not the creator, scrim accepts applications, and user hasn't applied already.
     *
     * @param scrim the Scrim entity
     * @param postulante the User applying
     * @throws IllegalStateException if validation fails
     */
    private void validatePostulationEligibility(Scrim scrim, User postulante) {
        if (scrim.getCreador().getId().equals(postulante.getId())) {
            throw new IllegalStateException("No puedes postularte a tu propio scrim.");
        }

        if (!"BUSCANDO_JUGADORES".equals(scrim.getEstado())) {
            throw new IllegalStateException("Este scrim no acepta postulaciones actualmente.");
        }

        if (postulationRepository.existsByScrimIdAndPostulanteId(scrim.getId(), postulante.getId())) {
            throw new IllegalStateException("Ya te has postulado a este scrim.");
        }
    }
}
