// Fichero: controller/ScrimController.java
package com.uade.tpo.Scrims.controller;

import com.uade.tpo.Scrims.model.domain.Postulation;
import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.service.ScrimService;
import com.uade.tpo.Scrims.view.dto.request.CreateScrimRequest;
import com.uade.tpo.Scrims.view.dto.request.FinalizeScrimRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; 

import com.uade.tpo.Scrims.view.dto.response.ScrimResponse;
import java.util.List; 

@RestController
@RequestMapping("/api/scrims")
public class ScrimController {

    @Autowired
    private ScrimService scrimService;

    @PostMapping
    public ResponseEntity<ScrimResponse> createScrim(@RequestBody CreateScrimRequest request, Principal principal) {
        String username = principal.getName();
        // El servicio ahora devuelve el DTO directamente
        ScrimResponse createdScrim = scrimService.createScrim(request, username);
        return ResponseEntity.status(201).body(createdScrim);
    }

    @PostMapping("/{scrimId}/postulations")
    public ResponseEntity<?> applyToScrim(@PathVariable Long scrimId, Principal principal) {
        try {
            Postulation postulation = scrimService.applyToScrim(scrimId, principal.getName());
            return ResponseEntity.status(201).body(postulation);
        } catch (RuntimeException e) {
            // Devolvemos un error claro si la postulación falla
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
            // Devolvemos un mensaje de error claro al cliente
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ScrimResponse>> getAllScrims() {
        List<ScrimResponse> scrims = scrimService.getAllScrims();
        return ResponseEntity.ok(scrims);
    }
}