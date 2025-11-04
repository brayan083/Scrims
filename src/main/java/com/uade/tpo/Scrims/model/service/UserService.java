// Fichero: model/service/UserService.java
package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.infrastructure.persistence.UserRepository;
import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.view.dto.request.LoginRequest;
import com.uade.tpo.Scrims.view.dto.request.RegisterUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired // <-- Inyectamos el PasswordEncoder
    private PasswordEncoder passwordEncoder;

    public User registerUser(RegisterUserRequest request) {
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        // AHORA HASHEAMOS LA CONTRASEÑA ANTES DE GUARDARLA
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(newUser);
    }

    public User login(LoginRequest request) {
        // Buscamos al usuario por su nombre de usuario
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado")); // <-- Lanza error si no lo encuentra

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) { // <-- Compara de forma segura
            return user;
        } else {
            throw new RuntimeException("Contraseña incorrecta"); // <-- Lanza error si no coincide
        }
        
    }
}
