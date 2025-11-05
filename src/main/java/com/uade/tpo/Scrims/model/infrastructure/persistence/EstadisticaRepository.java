// Fichero: model/infrastructure/persistence/EstadisticaRepository.java
package com.uade.tpo.Scrims.model.infrastructure.persistence;

import com.uade.tpo.Scrims.model.domain.Estadistica;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadisticaRepository extends JpaRepository<Estadistica, Long> {
    // Encuentra todas las estadísticas para un scrim específico
    List<Estadistica> findByScrimId(Long scrimId);
}