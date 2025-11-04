// Fichero: model/service/NotificationSubscriber.java
package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.patterns.observer.DomainEvent;
import com.uade.tpo.Scrims.model.patterns.observer.ScrimStateChangedEvent;
import com.uade.tpo.Scrims.model.patterns.observer.Subscriber;
import org.springframework.stereotype.Component;

@Component // Lo hacemos un bean para que Spring lo gestione
public class NotificationSubscriber implements Subscriber {

    @Override
    public void handleEvent(DomainEvent event) {
        // Verificamos si el evento es del tipo que nos interesa
        if (event instanceof ScrimStateChangedEvent stateEvent) {
            
            // Si el nuevo estado es LOBBY_ARMADO, enviamos la notificación
            if ("LOBBY_ARMADO".equals(stateEvent.nuevoEstado())) {
                System.out.println("---------------------------------------------------------");
                System.out.println("[OBSERVER] ¡Lobby Lleno! Notificando a jugadores del Scrim ID: " + stateEvent.scrim().getId());
                System.out.println("[OBSERVER] Mensaje: El lobby está completo. ¡Confirma tu participación!");
                System.out.println("---------------------------------------------------------");
                
                // TODO: Aquí iría la lógica real para enviar emails, push notifications, etc.
                // a cada uno de los jugadores del scrim.
            }
        }
    }
}