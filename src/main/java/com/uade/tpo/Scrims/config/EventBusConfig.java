// Fichero: config/EventBusConfig.java
package com.uade.tpo.Scrims.config;

import com.uade.tpo.Scrims.model.patterns.observer.DomainEventBus;
import com.uade.tpo.Scrims.model.service.NotificationSubscriber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBusConfig {

    @Autowired
    private DomainEventBus eventBus;

    @Autowired
    private NotificationSubscriber notificationSubscriber;

    @PostConstruct // Este método se ejecuta una vez que todas las dependencias se han inyectado
    public void registerSubscribers() {
        eventBus.subscribe(notificationSubscriber);
    }
}