package com.uade.tpo.Scrims.model.patterns.command;

/**
 * Patrón Command para operaciones de Scrim.
 * Encapsula solicitudes como objetos, permitiendo parametrizar y deshacer operaciones.
 */
public interface ScrimCommand {

    /**
     * Ejecuta la operación del comando.
     */
    Object execute();

    /**
     * Revierte la operación ejecutada.
     */
    Object undo();
}
