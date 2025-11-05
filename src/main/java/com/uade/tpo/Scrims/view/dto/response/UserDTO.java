// Fichero: view/dto/response/UserDTO.java
package com.uade.tpo.Scrims.view.dto.response;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Integer rango;
}