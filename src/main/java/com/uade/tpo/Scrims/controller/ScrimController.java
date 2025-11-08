
package com.uade.tpo.Scrims.controller;

import com.uade.tpo.Scrims.model.domain.Postulation;
import com.uade.tpo.Scrims.model.service.ScrimService;
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

@RestController
@RequestMapping("/api/scrims")
public class ScrimController {

    @Autowired
    private ScrimService scrimService;

    @PostMapping
    public ResponseEntity<ScrimResponse> createScrim(@RequestBody CreateScrimRequest request, Principal principal) {
        String username = principal.getName();
        ScrimResponse createdScrim = scrimService.createScrim(request, username);
        return ResponseEntity.status(201).body(createdScrim);
    }

    @PostMapping("/{scrimId}/postulations")
    public ResponseEntity<?> applyToScrim(@PathVariable Long scrimId, Principal principal) {
        try {
            Postulation postulation = scrimService.applyToScrim(scrimId, principal.getName());
            return ResponseEntity.status(201).body(postulation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{scrimId}/postulations/{postulationId}/accept")
    public ResponseEntity<?> acceptPostulation(
            @PathVariable Long scrimId,
            @PathVariable Long postulationId,
            Principal principal) {
        try {
            Postulation postulation = scrimService.acceptPostulation(scrimId, postulationId, principal.getName());
            return ResponseEntity.ok(postulation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{scrimId}/command")
    public ResponseEntity<?> executeScrimCommand(
            @PathVariable Long scrimId,
            @RequestBody CommandRequestDTO request,
            Principal principal) {
        try {
            Object result = scrimService.executeCommand(scrimId, request, principal.getName());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{scrimId}/confirmations")
    public ResponseEntity<?> confirmParticipation(@PathVariable Long scrimId, Principal principal) {
        try {
            scrimService.confirmParticipation(scrimId, principal.getName());
            return ResponseEntity.ok().body("Confirmación exitosa.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{scrimId}/finalize")
    public ResponseEntity<?> finalizeScrim(
            @PathVariable Long scrimId,
            @RequestBody FinalizeScrimRequest request,
            Principal principal) {
        try {
            scrimService.finalizeScrim(scrimId, request, principal.getName());
            return ResponseEntity.ok().body("Scrim finalizado y estadísticas cargadas correctamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{scrimId}/cancel")
    public ResponseEntity<?> cancelScrim(@PathVariable Long scrimId, Principal principal) {
        try {
            scrimService.cancelScrim(scrimId, principal.getName());
            return ResponseEntity.ok().body("Scrim cancelado exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{scrimId}/statistics")
    public ResponseEntity<?> getScrimStatistics(@PathVariable Long scrimId) {
        try {
            List<StatisticResponseDTO> stats = scrimService.getScrimStatistics(scrimId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ScrimResponse>> searchScrims(
            @RequestParam(required = false) String juego,
            @RequestParam(required = false) String formato,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String estado) {

        List<ScrimResponse> scrims = scrimService.searchScrims(juego, formato, region, estado);
        return ResponseEntity.ok(scrims);
    }

    @GetMapping("/my-scrims")
    public ResponseEntity<List<ScrimResponse>> getMyScrims(Principal principal) {
        List<ScrimResponse> myScrims = scrimService.findScrimsByCreator(principal.getName());
        return ResponseEntity.ok(myScrims);
    }

    @GetMapping("/my-participations")
    public ResponseEntity<List<ScrimResponse>> getMyParticipations(Principal principal) {
        List<ScrimResponse> participations = scrimService.findScrimsByParticipant(principal.getName());
        return ResponseEntity.ok(participations);
    }

    @GetMapping("/{scrimId}")
    public ResponseEntity<?> getScrimDetails(@PathVariable Long scrimId) {
        try {
            ScrimDetailResponseDTO scrimDetails = scrimService.getScrimDetails(scrimId);
            return ResponseEntity.ok(scrimDetails);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/{scrimId}/postulations")
    public ResponseEntity<?> getPostulationsForScrim(@PathVariable Long scrimId, Principal principal) {
        try {
            List<PostulationResponseDTO> postulations = scrimService.getPostulationsForScrim(scrimId, principal.getName());
            return ResponseEntity.ok(postulations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}