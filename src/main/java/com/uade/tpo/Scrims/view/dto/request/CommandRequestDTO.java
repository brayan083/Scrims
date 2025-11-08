package com.uade.tpo.Scrims.view.dto.request;

import lombok.Data;
import java.util.Map;

/**
 * DTO genérico para ejecutar comandos sobre scrims.
 */
@Data
public class CommandRequestDTO {
    /**
     * Identificador del comando a ejecutar (ej: "SWAP_PLAYERS", "KICK_PLAYER").
     */
    private String commandName;

    /**
     * Parámetros específicos del comando.
     * Ej: {"userId1": 1, "userId2": 5} o {"userIdToKick": 3, "reason": "No show"}
     */
    private Map<String, Object> payload;
}