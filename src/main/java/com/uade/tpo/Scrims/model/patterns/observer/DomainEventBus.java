// Fichero: model/patterns/observer/DomainEventBus.java
package com.uade.tpo.Scrims.model.patterns.observer;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component // Lo hacemos un bean de Spring para que sea un Singleton y se pueda inyectar
public class DomainEventBus {
    private final List<Subscriber> subscribers = new ArrayList<>();

    public void subscribe(Subscriber subscriber) {
        this.subscribers.add(subscriber);
        System.out.println("Nuevo suscriptor registrado: " + subscriber.getClass().getSimpleName());
    }

    public void publish(DomainEvent event) {
        System.out.println("Publicando evento: " + event.getClass().getSimpleName());
        for (Subscriber subscriber : subscribers) {
            subscriber.handleEvent(event);
        }
    }
}