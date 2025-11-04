// Fichero: controller/ScrimController.java
package com.uade.tpo.Scrims.controller;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.service.ScrimService;
import com.uade.tpo.Scrims.view.dto.request.CreateScrimRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; 

@RestController
@RequestMapping("/api/scrims")
public class ScrimController {

    @Autowired
    private ScrimService scrimService;

    @PostMapping
    // 1. Añadimos Principal principal como parámetro
    public ResponseEntity<Scrim> createScrim(@RequestBody CreateScrimRequest request, Principal principal) {
        // 2. Obtenemos el username del usuario autenticado
        String username = principal.getName();
        
        // 3. Pasamos el username al servicio
        Scrim createdScrim = scrimService.createScrim(request, username);
        
        return ResponseEntity.status(201).body(createdScrim);
    }
}