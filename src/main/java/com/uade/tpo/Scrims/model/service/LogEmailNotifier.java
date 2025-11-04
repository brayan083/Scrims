// Fichero: model/service/LogEmailNotifier.java (será nuestro "Adapter" falso)
package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.patterns.factory.Notifier;
import org.springframework.stereotype.Component;

@Component
public class LogEmailNotifier implements Notifier {
    @Override
    public void send(String destination, String message) {
        System.out.println("[DEV-EMAIL] Enviando a '" + destination + "': " + message);
    }
}