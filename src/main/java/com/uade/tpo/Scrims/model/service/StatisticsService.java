package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.domain.Estadistica;
import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.model.infrastructure.persistence.EstadisticaRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.ScrimRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.UserRepository;
import com.uade.tpo.Scrims.view.dto.request.PlayerStatisticDTO;
import com.uade.tpo.Scrims.view.dto.response.StatisticResponseDTO;
import com.uade.tpo.Scrims.view.mapper.ScrimMapper;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for managing player statistics within scrims.
 * Handles saving and retrieving statistics data.
 */
@Service
public class StatisticsService {
    private static final Logger log = LoggerFactory.getLogger(StatisticsService.class);

    @Autowired
    private EstadisticaRepository estadisticaRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ScrimRepository scrimRepository;
    @Autowired
    private ScrimMapper scrimMapper;

    /**
     * Saves player statistics for a scrim.
     * Creates and persists Estadistica entities based on the provided player stats.
     *
     * @param scrim the Scrim entity
     * @param playerStats list of player statistics to save
     */
    @Transactional
    public void savePlayerStatistics(Scrim scrim, List<PlayerStatisticDTO> playerStats) {
        for (PlayerStatisticDTO statDTO : playerStats) {
            User player = userRepository.findById(statDTO.getUserId())
                    .orElseThrow(() -> new RuntimeException("Jugador con ID " + statDTO.getUserId() + " no encontrado."));

            Estadistica estadistica = new Estadistica();
            estadistica.setScrim(scrim);
            estadistica.setUser(player);
            estadistica.setKda(statDTO.getKda());
            estadistica.setMvp(statDTO.isMvp());

            estadisticaRepository.save(estadistica);
            log.debug("Estadística guardada para el jugador: {}", player.getUsername());
        }
        
        log.info("Estadísticas guardadas para {} jugadores en scrim {}", playerStats.size(), scrim.getId());
    }

    /**
     * Retrieves all statistics for a finalized scrim.
     * Only available for scrims with FINALIZADO status.
     *
     * @param scrimId the ID of the scrim
     * @return list of StatisticResponseDTO
     * @throws RuntimeException if scrim not found
     * @throws IllegalStateException if scrim is not finalized
     */
    public List<StatisticResponseDTO> getScrimStatistics(Long scrimId) {
        Scrim scrim = scrimRepository.findById(scrimId)
                .orElseThrow(() -> new RuntimeException("Scrim no encontrado con ID: " + scrimId));

        if (!"FINALIZADO".equals(scrim.getEstado())) {
            throw new IllegalStateException("Las estadísticas solo están disponibles para scrims finalizados.");
        }

        List<Estadistica> stats = estadisticaRepository.findByScrimId(scrimId);

        return stats.stream()
                .map(scrimMapper::toStatisticResponse)
                .collect(Collectors.toList());
    }
}
