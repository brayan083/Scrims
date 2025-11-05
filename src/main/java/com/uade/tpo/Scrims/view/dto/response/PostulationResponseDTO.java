// Fichero: view/dto/response/PostulationResponseDTO.java
package com.uade.tpo.Scrims.view.dto.response;

import lombok.Data;

@Data
public class PostulationResponseDTO {
    private Long postulationId; // El ID de la postulación, crucial para aceptarla/rechazarla
    private Long userId;        // El ID del usuario que se postuló
    private String username;    // El nombre del usuario
    private Integer userRank;   // El rango del usuario para que el creador pueda decidir
    private String status;      // "PENDIENTE", "ACEPTADA", etc.
}