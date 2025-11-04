// Fichero: model/patterns/observer/Subscriber.java
package com.uade.tpo.Scrims.model.patterns.observer;

public interface Subscriber {
    void handleEvent(DomainEvent event);
}