package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.domain.Postulation;
import com.uade.tpo.Scrims.view.dto.request.CreateScrimRequest;
import com.uade.tpo.Scrims.view.dto.request.FinalizeScrimRequest;
import com.uade.tpo.Scrims.view.dto.response.ScrimResponse;

/**
 * Facade pattern interface for Scrim operations.
 * Coordinates complex operations that involve multiple specialized services.
 * Use this facade when an operation requires interaction between different services.
 * For simple operations, use the specialized services directly.
 */
public interface ScrimFacade {

    /**
     * Complex operation: Create a scrim with initial team setup.
     * Coordinates ScrimService (scrim creation) and TeamManagementService (team setup).
     *
     * @param request the create scrim request
     * @param username the username of the creator
     * @return the created ScrimResponse
     */
    ScrimResponse createScrimWithTeams(CreateScrimRequest request, String username);

    /**
     * Complex operation: Apply to scrim and potentially auto-accept based on strategy.
     * Coordinates PostulationService (application) and TeamManagementService (lobby management).
     *
     * @param scrimId the ID of the scrim
     * @param username the username of the applicant
     * @return the created Postulation
     */
    Postulation applyToScrimWithAutoAccept(Long scrimId, String username);

    /**
     * Complex operation: Finalize scrim with statistics and MMR updates.
     * Coordinates:
     * - ScrimFinalizationService (finalization and MMR)
     * - StatisticsService (statistics saving) - already called internally
     *
     * @param scrimId the ID of the scrim
     * @param request the finalization request
     * @param username the username of the creator
     */
    void finalizeScrimComplete(Long scrimId, FinalizeScrimRequest request, String username);

    /**
     * Future complex operation example: Cancel scrim with notifications and refunds.
     * This demonstrates how the facade could coordinate multiple services for a complex operation.
     * Currently just delegates to ScrimService, but could be extended to:
     * - Send notifications to all participants
     * - Process refunds if applicable
     * - Update player statistics
     *
     * @param scrimId the ID of the scrim
     * @param username the username of the creator
     */
    void cancelScrimWithCleanup(Long scrimId, String username);
}
