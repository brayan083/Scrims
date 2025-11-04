// Fichero: model/service/LogDiscordNotifier.java
package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.patterns.factory.Notifier;
import org.springframework.stereotype.Component;

@Component
public class LogDiscordNotifier implements Notifier {
    @Override
    public void send(String destination, String message) {
        System.out.println("[DEV-DISCORD] Enviando a canal '" + destination + "': " + message);
    }
}