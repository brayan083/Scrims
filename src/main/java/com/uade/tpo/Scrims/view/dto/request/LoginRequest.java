// Fichero: view/dto/request/LoginRequest.java
package com.uade.tpo.Scrims.view.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}