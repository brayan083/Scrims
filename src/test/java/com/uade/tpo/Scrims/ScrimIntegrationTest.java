// Fichero: src/test/java/com/uade/tpo/Scrims/ScrimIntegrationTest.java
package com.uade.tpo.Scrims;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.tpo.Scrims.view.dto.request.CreateScrimRequest;
import com.uade.tpo.Scrims.view.dto.request.RegisterUserRequest;
import com.uade.tpo.Scrims.view.dto.response.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // Configura MockMvc automáticamente
public class ScrimIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Objeto para simular las llamadas HTTP

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos Java a JSON

    @Test
    void testFullScrimCreationFlow() throws Exception {
        // --- 1. Registrar un nuevo usuario ---
        RegisterUserRequest registerRequest = new RegisterUserRequest();
        registerRequest.setUsername("integration_tester");
        registerRequest.setEmail("integration@test.com");
        registerRequest.setPassword("password123");
        registerRequest.setRango(10);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk()); // Verificamos que la respuesta es 200 OK

        // --- 2. Iniciar sesión para obtener el token ---
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"integration_tester\", \"password\": \"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists()) // Verificamos que la respuesta contiene un token
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseBody, LoginResponse.class);
        String token = loginResponse.getToken();

        // --- 3. Crear un Scrim usando el token ---
        CreateScrimRequest scrimRequest = new CreateScrimRequest();
        scrimRequest.setJuego("Integration Test Game");
        scrimRequest.setFormato("1v1");
        scrimRequest.setRegion("TEST");
        scrimRequest.setRangoMin(5);
        scrimRequest.setRangoMax(15);
        scrimRequest.setFechaHora(LocalDateTime.now().plusHours(1));

        mockMvc.perform(post("/api/scrims")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token) // ¡Usamos el token!
                .content(objectMapper.writeValueAsString(scrimRequest)))
                .andExpect(status().isCreated()) // Verificamos que la respuesta es 201 Created
                .andExpect(jsonPath("$.id").exists()) // Verificamos que el scrim creado tiene un ID
                .andExpect(jsonPath("$.creadorUsername").value("integration_tester")); // Verificamos que el creador es correcto
    }
}