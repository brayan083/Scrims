// Fichero: view/dto/response/TeamMemberResponseDTO.java
package com.uade.tpo.Scrims.view.dto.response;

import lombok.Data;

@Data
public class TeamMemberResponseDTO {
    private Long userId;
    private String username;
    private Integer userRank;
    private boolean haConfirmado; 
}