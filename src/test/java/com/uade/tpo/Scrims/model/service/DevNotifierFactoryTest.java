// Fichero: src/test/java/.../model/service/DevNotifierFactoryTest.java


package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.patterns.factory.Notifier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // Le dice a Spring que cargue el contexto completo de la aplicación para esta prueba
class DevNotifierFactoryTest {

    @Autowired // Inyectamos la fábrica que queremos probar, Spring se encarga de crearla
    private DevNotifierFactory devNotifierFactory;

    @Test
    void testCreateEmailNotifier_ShouldReturnLogEmailNotifier() {
        // Act: Llamamos al método que queremos probar
        Notifier notifier = devNotifierFactory.createEmailNotifier();

        // Assert: Verificamos que el resultado sea el esperado
        assertNotNull(notifier, "El notificador no debería ser nulo.");
        assertTrue(notifier instanceof LogEmailNotifier, "El notificador debería ser una instancia de LogEmailNotifier.");
    }

    @Test
    void testCreateDiscordNotifier_ShouldReturnLogDiscordNotifier() {
        // Act
        Notifier notifier = devNotifierFactory.createDiscordNotifier();

        // Assert
        assertNotNull(notifier);
        assertTrue(notifier instanceof LogDiscordNotifier);
    }
}


