// Fichero: view/dto/request/FinalizeScrimRequest.java
package com.uade.tpo.Scrims.view.dto.request;

import lombok.Data;
import java.util.List; // <-- Importante añadir esta línea

@Data
public class FinalizeScrimRequest {
    
    // Este es el campo que faltaba.
    // Ahora esperamos una lista de estadísticas, una por cada jugador.
    private List<PlayerStatisticDTO> playerStats; 

    // Mantenemos el campo para el resultado general de la partida.
    private String resultado; 
}