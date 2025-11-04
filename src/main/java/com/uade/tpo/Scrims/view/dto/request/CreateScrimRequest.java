// Fichero: view/dto/request/CreateScrimRequest.java
package com.uade.tpo.Scrims.view.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateScrimRequest {
    private String juego;
    private String formato;
    private String region;
    private Integer rangoMin;
    private Integer rangoMax;
    private LocalDateTime fechaHora;
}