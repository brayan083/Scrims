// Fichero: view/dto/request/PlayerStatisticDTO.java
package com.uade.tpo.Scrims.view.dto.request;

import lombok.Data;

@Data
public class PlayerStatisticDTO {
    private Long userId;    // El ID del jugador
    private String kda;     // El KDA como un String, ej: "15/3/8"
    private boolean mvp;    // Si fue el MVP de la partida
}