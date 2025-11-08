package com.uade.tpo.Scrims.model.patterns.strategy;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.User;

/**
 * Estrategia de matchmaking que determina cómo se procesan las postulaciones a un scrim.
 * Retorna: "PENDIENTE", "ACEPTADA" o "RECHAZADA"
 */
public interface MatchmakingStrategy {
    String procesarPostulacion(User postulante, Scrim scrim);
}