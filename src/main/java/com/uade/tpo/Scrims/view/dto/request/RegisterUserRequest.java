// Fichero: view/dto/request/RegisterUserRequest.java
package com.uade.tpo.Scrims.view.dto.request;

import lombok.Data;

@Data
public class RegisterUserRequest {
    private String username;
    private String email;
    private String password;
}