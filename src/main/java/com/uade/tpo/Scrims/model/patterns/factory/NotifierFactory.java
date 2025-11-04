// Fichero: model/patterns/factory/NotifierFactory.java
package com.uade.tpo.Scrims.model.patterns.factory;

public interface NotifierFactory {
    Notifier createEmailNotifier();
    Notifier createDiscordNotifier();
    // Podríamos añadir createPushNotifier(), etc.
}