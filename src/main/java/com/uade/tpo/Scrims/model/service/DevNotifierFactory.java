// Fichero: model/service/DevNotifierFactory.java
package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.patterns.factory.Notifier;
import com.uade.tpo.Scrims.model.patterns.factory.NotifierFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("devNotifierFactory") // Le damos un nombre al bean
public class DevNotifierFactory implements NotifierFactory {
    @Autowired
    private LogEmailNotifier logEmailNotifier;
    @Autowired
    private LogDiscordNotifier logDiscordNotifier;

    @Override
    public Notifier createEmailNotifier() {
        return logEmailNotifier;
    }

    @Override
    public Notifier createDiscordNotifier() {
        return logDiscordNotifier;
    }
}