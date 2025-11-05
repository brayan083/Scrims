// Fichero: model/service/AuthService.java
package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.model.infrastructure.persistence.UserRepository;
import com.uade.tpo.Scrims.view.dto.request.LoginRequest;
import com.uade.tpo.Scrims.view.dto.request.RegisterUserRequest;
import com.uade.tpo.Scrims.view.dto.response.LoginResponse;
import com.uade.tpo.Scrims.view.dto.response.UserDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;

    // Definimos una constante para el MMR inicial.
    private static final int INITIAL_MMR = 10;

    public LoginResponse register(RegisterUserRequest request) {
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // Asignamos el MMR inicial, ignorando lo que venga en el request.
        newUser.setRango(INITIAL_MMR);

        UserDTO userDTO = mapUserToDTO(newUser);

        userRepository.save(newUser);

        var jwtToken = jwtService.generateToken(newUser);
        return LoginResponse.builder().token(jwtToken).user(userDTO).build();
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Creamos el DTO del usuario para la respuesta
        UserDTO userDTO = mapUserToDTO(user);
        var jwtToken = jwtService.generateToken(user);
        return LoginResponse.builder().token(jwtToken).user(userDTO).build();
    }

    // Método de ayuda privado para convertir la entidad User a UserDTO
    private UserDTO mapUserToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setRango(user.getRango());
        return userDTO;
    }
}