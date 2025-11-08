package com.uade.tpo.Scrims.model.patterns.strategy;

import com.uade.tpo.Scrims.model.domain.Scrim;
import com.uade.tpo.Scrims.model.domain.User;
import org.springframework.stereotype.Component;

/**
 * Estrategia automática: acepta o rechaza postulaciones según el rango MMR del jugador.
 */
@Component("BY_MMR")
public class ByMMRStrategy implements MatchmakingStrategy {

    @Override
    public String procesarPostulacion(User postulante, Scrim scrim) {
        if (postulante.getRango() == null) {
            System.out.println("[Strategy.ByMMR] Rechazado: " + postulante.getUsername() + " no tiene rango.");
            return "RECHAZADA";
        }

        Integer rangoJugador = postulante.getRango();
        Integer rangoMin = scrim.getRangoMin();
        Integer rangoMax = scrim.getRangoMax();

        if (rangoJugador >= rangoMin && rangoJugador <= rangoMax) {
            System.out.println("[Strategy.ByMMR] Aceptado: " + postulante.getUsername() + " (Rango: " + rangoJugador + ") está dentro de [" + rangoMin + "-" + rangoMax + "].");
            return "ACEPTADA";
        } else {
            System.out.println("[Strategy.ByMMR] Rechazado: " + postulante.getUsername() + " (Rango: " + rangoJugador + ") está fuera de [" + rangoMin + "-" + rangoMax + "].");
            return "RECHAZADA";
        }
    }
}