// Fichero: model/service/ScrimService.java
package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.model.infrastructure.persistence.ScrimRepository;
import com.uade.tpo.Scrims.model.infrastructure.persistence.UserRepository;
import com.uade.tpo.Scrims.view.dto.request.CreateScrimRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScrimService {
    @Autowired
    private ScrimRepository scrimRepository;
    @Autowired
    private UserRepository userRepository;

    // 1. La firma del método ahora acepta un 'username'
    public Scrim createScrim(CreateScrimRequest request, String username) {
        // 2. Buscamos al usuario por su username, que es seguro y viene del token
        User creador = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Creador no encontrado. El token puede ser inválido."));

        Scrim newScrim = new Scrim();
        newScrim.setJuego(request.getJuego());
        newScrim.setFormato(request.getFormato());
        newScrim.setRegion(request.getRegion());
        newScrim.setRangoMin(request.getRangoMin());
        newScrim.setRangoMax(request.getRangoMax());
        newScrim.setFechaHora(request.getFechaHora());
        newScrim.setEstado("BUSCANDO_JUGADORES");
        newScrim.setCreador(creador); // Asignamos el creador correcto

        return scrimRepository.save(newScrim);
    }
}