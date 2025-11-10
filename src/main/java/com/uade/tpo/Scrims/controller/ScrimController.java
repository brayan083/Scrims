
package com.uade.tpo.Scrims.controller;

import com.uade.tpo.Scrims.model.domain.Postulation;
import com.uade.tpo.Scrims.model.service.ScrimFacade;
import com.uade.tpo.Scrims.model.service.ScrimService;
import com.uade.tpo.Scrims.model.service.PostulationService;
import com.uade.tpo.Scrims.model.service.TeamManagementService;
import com.uade.tpo.Scrims.model.service.ScrimFinalizationService;
import com.uade.tpo.Scrims.model.service.StatisticsService;
import com.uade.tpo.Scrims.view.dto.request.CommandRequestDTO;
import com.uade.tpo.Scrims.view.dto.request.CreateScrimRequest;
import com.uade.tpo.Scrims.view.dto.request.FinalizeScrimRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import com.uade.tpo.Scrims.view.dto.response.PostulationResponseDTO;
import com.uade.tpo.Scrims.view.dto.response.ScrimDetailResponseDTO;
import com.uade.tpo.Scrims.view.dto.response.ScrimResponse;
import com.uade.tpo.Scrims.view.dto.response.StatisticResponseDTO;

import java.util.List;

/**
 * REST Controller for Scrim management operations.
 * 
 * Architecture Pattern: Uses Facade pattern for complex operations and direct service injection for simple operations.
 * 
 * - Use ScrimFacade for complex operations that coordinate multiple services:
 *   - Create scrim (coordinates scrim creation + team setup)
 *   - Apply to scrim (coordinates application + auto-acceptance)
 *   - Finalize scrim (coordinates finalization + statistics + MMR)
 *   - Cancel scrim (coordinates cancellation + cleanup)
 * 
 * - Use specialized services directly for simple operations:
 *   - PostulationService: Accept postulation, get postulations list
 *   - TeamManagementService: Execute commands
 *   - ScrimFinalizationService: Confirm participation
 *   - StatisticsService: Get statistics
 *   - ScrimService: Search, get details, find by creator/participant
 */
@RestController
@RequestMapping("/api/scrims")
public class ScrimController {

    // Facade for complex operations
    @Autowired
    private ScrimFacade scrimFacade;

    // Core service for scrim lifecycle and queries
    @Autowired
    private ScrimService scrimService;

    // Specialized services for direct simple operations
    @Autowired
    private PostulationService postulationService;

    @Autowired
    private TeamManagementService teamManagementService;

    @Autowired
    private ScrimFinalizationService scrimFinalizationService;

    @Autowired
    private StatisticsService statisticsService;

    @PostMapping
    public ResponseEntity<ScrimResponse> createScrim(@RequestBody CreateScrimRequest request, Principal principal) {
        String username = principal.getName();
        // Complex operation: Uses Facade to coordinate scrim creation + team setup
        ScrimResponse createdScrim = scrimFacade.createScrimWithTeams(request, username);
        return ResponseEntity.status(201).body(createdScrim);
    }

    @PostMapping("/{scrimId}/postulations")
    public ResponseEntity<Postulation> applyToScrim(@PathVariable Long scrimId, Principal principal) {
        // Complex operation: Uses Facade to coordinate application + auto-acceptance
        Postulation postulation = scrimFacade.applyToScrimWithAutoAccept(scrimId, principal.getName());
        return ResponseEntity.status(201).body(postulation);
    }

    @PostMapping("/{scrimId}/postulations/{postulationId}/accept")
    public ResponseEntity<Postulation> acceptPostulation(
            @PathVariable Long scrimId,
            @PathVariable Long postulationId,
            Principal principal) {
        // Simple operation: Direct service call
        Postulation postulation = postulationService.acceptPostulation(scrimId, postulationId, principal.getName());
        return ResponseEntity.ok(postulation);
    }

    @PostMapping("/{scrimId}/command")
    public ResponseEntity<Object> executeScrimCommand(
            @PathVariable Long scrimId,
            @RequestBody CommandRequestDTO request,
            Principal principal) {
        // Simple operation: Direct service call for command execution
        Object result = teamManagementService.executeCommand(scrimId, request, principal.getName());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{scrimId}/confirmations")
    public ResponseEntity<String> confirmParticipation(@PathVariable Long scrimId, Principal principal) {
        // Simple operation: Direct service call
        scrimFinalizationService.confirmParticipation(scrimId, principal.getName());
        return ResponseEntity.ok("Confirmación exitosa.");
    }

    @PostMapping("/{scrimId}/finalize")
    public ResponseEntity<String> finalizeScrim(
            @PathVariable Long scrimId,
            @RequestBody FinalizeScrimRequest request,
            Principal principal) {
        // Complex operation: Uses Facade to coordinate finalization + statistics + MMR
        scrimFacade.finalizeScrimComplete(scrimId, request, principal.getName());
        return ResponseEntity.ok("Scrim finalizado y estadísticas cargadas correctamente.");
    }

    @PostMapping("/{scrimId}/cancel")
    public ResponseEntity<String> cancelScrim(@PathVariable Long scrimId, Principal principal) {
        // Complex operation: Uses Facade to coordinate cancellation + cleanup
        scrimFacade.cancelScrimWithCleanup(scrimId, principal.getName());
        return ResponseEntity.ok("Scrim cancelado exitosamente.");
    }

    @GetMapping("/{scrimId}/statistics")
    public ResponseEntity<List<StatisticResponseDTO>> getScrimStatistics(@PathVariable Long scrimId) {
        // Simple operation: Direct service call
        List<StatisticResponseDTO> stats = statisticsService.getScrimStatistics(scrimId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping
    public ResponseEntity<List<ScrimResponse>> searchScrims(
            @RequestParam(required = false) String juego,
            @RequestParam(required = false) String formato,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String estado) {

        // Simple operation: Direct service call for queries
        List<ScrimResponse> scrims = scrimService.searchScrims(juego, formato, region, estado);
        return ResponseEntity.ok(scrims);
    }

    @GetMapping("/my-scrims")
    public ResponseEntity<List<ScrimResponse>> getMyScrims(Principal principal) {
        // Simple operation: Direct service call for queries
        List<ScrimResponse> myScrims = scrimService.findScrimsByCreator(principal.getName());
        return ResponseEntity.ok(myScrims);
    }

    @GetMapping("/my-participations")
    public ResponseEntity<List<ScrimResponse>> getMyParticipations(Principal principal) {
        // Simple operation: Direct service call for queries
        List<ScrimResponse> participations = scrimService.findScrimsByParticipant(principal.getName());
        return ResponseEntity.ok(participations);
    }

    @GetMapping("/{scrimId}")
    public ResponseEntity<ScrimDetailResponseDTO> getScrimDetails(@PathVariable Long scrimId) {
        // Simple operation: Direct service call for queries
        ScrimDetailResponseDTO scrimDetails = scrimService.getScrimDetails(scrimId);
        return ResponseEntity.ok(scrimDetails);
    }

    @GetMapping("/{scrimId}/postulations")
    public ResponseEntity<List<PostulationResponseDTO>> getPostulationsForScrim(@PathVariable Long scrimId, Principal principal) {
        // Simple operation: Direct service call
        List<PostulationResponseDTO> postulations = postulationService.getPostulationsForScrim(scrimId, principal.getName());
        return ResponseEntity.ok(postulations);
    }
}