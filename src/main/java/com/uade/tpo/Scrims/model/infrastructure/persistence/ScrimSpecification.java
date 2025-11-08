// Fichero: model/infrastructure/persistence/ScrimSpecification.java
package com.uade.tpo.Scrims.model.infrastructure.persistence;

import com.uade.tpo.Scrims.model.domain.Scrim;
import org.springframework.data.jpa.domain.Specification;

public class ScrimSpecification {

    // Método auxiliar para crear una especificación base vacía
    public static Specification<Scrim> empty() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    // Devuelve una "condición" que JPA usará para filtrar por juego
    public static Specification<Scrim> tieneJuego(String juego) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("juego"), juego);
    }

    // Devuelve una "condición" para filtrar por formato
    public static Specification<Scrim> tieneFormato(String formato) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("formato"), formato);
    }

    // Devuelve una "condición" para filtrar por región
    public static Specification<Scrim> tieneRegion(String region) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("region"), region);
    }
    
    // Devuelve una "condición" para filtrar por estado
    public static Specification<Scrim> tieneEstado(String estado) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("estado"), estado);
    }
}