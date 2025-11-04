// Fichero: controller/AuthController.java
package com.uade.tpo.Scrims.controller;

import com.uade.tpo.Scrims.model.service.AuthService;
import com.uade.tpo.Scrims.view.dto.request.LoginRequest;
import com.uade.tpo.Scrims.view.dto.request.RegisterUserRequest;
import com.uade.tpo.Scrims.view.dto.response.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> registerUser(@RequestBody RegisterUserRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}