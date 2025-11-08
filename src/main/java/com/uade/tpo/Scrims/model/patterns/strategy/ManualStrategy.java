package com.uade.tpo.Scrims.model.patterns.strategy;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.User;
import org.springframework.stereotype.Component;

/**
 * Estrategia manual: todas las postulaciones requieren revisión del organizador.
 */
@Component("MANUAL")
public class ManualStrategy implements MatchmakingStrategy {

    @Override
    public String procesarPostulacion(User postulante, Scrim scrim) {
        return "PENDIENTE";
    }
}