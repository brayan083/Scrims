// Fichero: model/service/NotificationSubscriber.java
package com.uade.tpo.Scrims.model.service;

import com.uade.tpo.Scrims.model.domain.Team;
import com.uade.tpo.Scrims.model.domain.User;
import com.uade.tpo.Scrims.model.infrastructure.persistence.TeamRepository;
import com.uade.tpo.Scrims.model.patterns.factory.Notifier;
import com.uade.tpo.Scrims.model.patterns.factory.NotifierFactory;
import com.uade.tpo.Scrims.model.patterns.observer.DomainEvent;
import com.uade.tpo.Scrims.model.patterns.observer.ScrimStateChangedEvent;
import com.uade.tpo.Scrims.model.patterns.observer.Subscriber;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component // Lo hacemos un bean para que Spring lo gestione
public class NotificationSubscriber implements Subscriber {

    @Autowired
    @Qualifier("devNotifierFactory") // Le decimos a Spring cuál de las fábricas usar
    private NotifierFactory notifierFactory;

    @Autowired
    private TeamRepository teamRepository;

    @Override
    public void handleEvent(DomainEvent event) {
        if (event instanceof ScrimStateChangedEvent stateEvent) {
            if ("LOBBY_ARMADO".equals(stateEvent.nuevoEstado())) {
                System.out.println("[OBSERVER] Evento LOBBY_ARMADO detectado. Usando fábrica para notificar.");

                // 1. Usamos la fábrica para crear los notificadores que necesitamos
                Notifier emailNotifier = notifierFactory.createEmailNotifier();
                Notifier discordNotifier = notifierFactory.createDiscordNotifier();

                String message = "El lobby para el scrim de " + stateEvent.scrim().getJuego()
                        + " está completo. ¡Confirma tu participación!";

                // --- ¡LÓGICA MEJORADA! ---
                // 1. Obtenemos los equipos del scrim
                List<Team> teams = teamRepository.findByScrimId(stateEvent.scrim().getId());
                // 2. Obtenemos todos los miembros de todos los equipos y los juntamos en una
                // sola lista
                List<User> todosLosJugadores = teams.stream()
                        .map(Team::getMiembros)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());

                // 3. Iteramos y notificamos a cada jugador real
                for (User jugador : todosLosJugadores) {
                    emailNotifier.send(jugador.getEmail(), message);
                    // Para discord, necesitaríamos el ID de Discord del usuario. Por ahora,
                    // simulamos.
                    discordNotifier.send("discord_id_" + jugador.getUsername(), message);
                }
            }
        }
    }
}