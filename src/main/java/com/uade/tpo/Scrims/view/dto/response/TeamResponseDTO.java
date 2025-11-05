// Fichero: view/dto/response/TeamResponseDTO.java
package com.uade.tpo.Scrims.view.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class TeamResponseDTO {
    private Long id;
    private String nombre;
    private List<TeamMemberResponseDTO> miembros;
}