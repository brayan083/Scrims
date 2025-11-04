// Fichero: view/dto/response/ScrimResponse.java
package com.uade.tpo.Scrims.view.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScrimResponse {
    private Long id;
    private String juego;
    private String formato;
    private String region;
    private Integer rangoMin;
    private Integer rangoMax;
    private LocalDateTime fechaHora;
    private String estado;
    private String creadorUsername; // Solo mostraremos el nombre de usuario, no todo el objeto
}