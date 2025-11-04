// Fichero: model/infrastructure/persistence/PostulationRepository.java
package com.uade.tpo.Scrims.model.infrastructure.persistence;

import com.uade.tpo.Scrims.model.domain.Postulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostulationRepository extends JpaRepository<Postulation, Long> {
    // Devuelve true si ya existe una fila con este scrimId y este userId
    boolean existsByScrimIdAndPostulanteId(Long scrimId, Long postulanteId);
}