// Fichero: model/patterns/observer/ScrimStateChangedEvent.java
package com.uade.tpo.Scrims.model.patterns.observer;

import com.uade.tpo.Scrims.model.domain.Scrim;

// Este evento se dispara cuando el estado de un scrim cambia.
public record ScrimStateChangedEvent(Scrim scrim, String nuevoEstado) implements DomainEvent {
}