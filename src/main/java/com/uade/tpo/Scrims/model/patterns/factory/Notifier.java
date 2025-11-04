// Fichero: model/patterns/factory/Notifier.java
package com.uade.tpo.Scrims.model.patterns.factory;

public interface Notifier {
    void send(String destination, String message);
}