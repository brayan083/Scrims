// Fichero: model/infrastructure/persistence/ScrimRepository.java
package com.uade.tpo.Scrims.model.infrastructure.persistence;

import com.uade.tpo.Scrims.model.domain.Scrim;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrimRepository extends JpaRepository<Scrim, Long> {
    List<Scrim> findByEstado(String estado);
}