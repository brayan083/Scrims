package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.domain.Postulation;
import com.uade.tpo.Scrims.view.dto.request.CreateScrimRequest;
import com.uade.tpo.Scrims.view.dto.request.FinalizeScrimRequest;
import com.uade.tpo.Scrims.view.dto.response.ScrimResponse;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation of ScrimFacade.
 * Coordinates complex operations between multiple specialized services.
 */
@Service
public class ScrimFacadeImpl implements ScrimFacade {
    private static final Logger log = LoggerFactory.getLogger(ScrimFacadeImpl.class);

    @Autowired
    private ScrimService scrimService;
    @Autowired
    private PostulationService postulationService;
    @Autowired
    private ScrimFinalizationService scrimFinalizationService;

    @Override
    @Transactional
    public ScrimResponse createScrimWithTeams(CreateScrimRequest request, String username) {
        log.info("Facade: Creando scrim con equipos para usuario {}", username);
        // ScrimService already handles team creation internally
        // This is kept for consistency with the facade pattern
        return scrimService.createScrim(request, username);
    }

    @Override
    @Transactional
    public Postulation applyToScrimWithAutoAccept(Long scrimId, String username) {
        log.info("Facade: Procesando postulación de {} a scrim {}", username, scrimId);
        // PostulationService already handles auto-acceptance coordination
        return postulationService.applyToScrim(scrimId, username);
    }

    @Override
    @Transactional
    public void finalizeScrimComplete(Long scrimId, FinalizeScrimRequest request, String username) {
        log.info("Facade: Finalizando scrim {} completamente", scrimId);
        // ScrimFinalizationService already coordinates with StatisticsService
        scrimFinalizationService.finalizeScrim(scrimId, request, username);
    }

    @Override
    @Transactional
    public void cancelScrimWithCleanup(Long scrimId, String username) {
        log.info("Facade: Cancelando scrim {} con limpieza completa", scrimId);
        scrimService.cancelScrim(scrimId, username);
        // Future: Add notification service call
        // Future: Add refund processing
        // Future: Add statistics cleanup
    }
}
