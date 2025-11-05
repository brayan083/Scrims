// Fichero: view/dto/response/ScrimDetailResponseDTO.java
package com.uade.tpo.Scrims.view.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScrimDetailResponseDTO {
    // Datos básicos del Scrim
    private Long id;
    private String juego;
    private String formato;
    private String region;
    private Integer rangoMin;
    private Integer rangoMax;
    private LocalDateTime fechaHora;
    private String estado;
    private String creadorUsername;

    // Datos anidados de los equipos
    private List<TeamResponseDTO> teams;
}