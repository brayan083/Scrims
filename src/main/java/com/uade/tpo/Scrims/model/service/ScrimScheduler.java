// Fichero: model/service/ScrimScheduler.java
package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.infrastructure.persistence.ScrimRepository;
import com.uade.tpo.Scrims.model.patterns.state.EnJuegoState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScrimScheduler {
    @Autowired
    private ScrimRepository scrimRepository;

    // Se ejecuta cada 60 segundos. fixedRate está en milisegundos.
    @Scheduled(fixedRate = 60000) 
    public void checkScrimsToStart() {
        System.out.println("[SCHEDULER] Verificando scrims para iniciar...");
        
        // Buscamos scrims que estén en estado CONFIRMADO
        List<Scrim> scrimsConfirmados = scrimRepository.findByEstado("CONFIRMADO");

        for (Scrim scrim : scrimsConfirmados) {
            // Si la fechaHora del scrim es anterior o igual a la hora actual
            if (!scrim.getFechaHora().isAfter(LocalDateTime.now())) {
                System.out.println("[SCHEDULER] ¡Iniciando Scrim ID: " + scrim.getId() + "! Transicionando a EN_JUEGO.");
                scrim.setState(new EnJuegoState());
                scrimRepository.save(scrim);

                // TODO: Aquí también se podría publicar un evento para notificar a los jugadores.
            }
        }
    }
}