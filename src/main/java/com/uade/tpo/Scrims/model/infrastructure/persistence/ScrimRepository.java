// Fichero: model/infrastructure/persistence/ScrimRepository.java
package com.uade.tpo.Scrims.model.infrastructure.persistence;

import com.uade.tpo.Scrims.model.domain.Scrim;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// import org.springframework.data.jpa.domain.Specification; // <-- AÑADIR (opcional)
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <-- AÑADIR
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ScrimRepository extends JpaRepository<Scrim, Long>, JpaSpecificationExecutor<Scrim> {
    List<Scrim> findByEstado(String estado);
    List<Scrim> findByCreadorIdOrderByFechaHoraDesc(Long creatorId);

    @Query("SELECT DISTINCT t.scrim FROM Team t JOIN t.miembros m WHERE m.id = :userId ORDER BY t.scrim.fechaHora DESC")
    List<Scrim> findScrimsByParticipantId(@Param("userId") Long userId);

    @Query("SELECT s FROM Scrim s JOIN FETCH s.creador WHERE s IN :scrims")
    List<Scrim> findAllWithCreador(@Param("scrims") List<Scrim> scrims);
}